package com.holeinone.ssafit.model.service;

import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.holeinone.ssafit.model.dao.VideoDao;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    private VideoDao videoDao;

    private final String apiKey;

    // application.properties에 넣어둔 값 자바 변수로 넣어줌
    public VideoServiceImpl(@Value("${youtube.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    // 유튜브 영상 검색
    @Override
    public List<String> searchVideos(String part, String duration, String recommend) {

        List<String> videos = new ArrayList<>();  // 비디오 정보를 저장할 리스트

        System.out.println(part);

        try {
            // YouTube API 서비스 객체를 생성 (Google API와 연결)
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),  // Google API 서버와 통신할 HTTP 연결
                    GsonFactory.getDefaultInstance(),  // 응답 JSON 데이터를 파싱할 GsonFactory
                    null  // 인증이 필요하지 않음 (API 키로 인증)
            ).setApplicationName("holeinone-ssafit").build();  // Google API에 애플리케이션 이름 명시, YouTube 서비스 객체 생성

            // 유튜브 검색 요청을 생성
            YouTube.Search.List searchRequest = youtubeService.search()
                    .list(List.of("snippet"))  // 검색할 데이터 종류 설정 ('snippet'은 제목, 설명 등 기본 정보)
                    .setQ(part)  // part 값으로 검색
                    .setType(List.of("video"))  // 검색 결과 중 비디오만 필터링
                    .setMaxResults(300L)  // 최대 30개의 결과 가져오기 (필터링 때문에 여유있게 가져옴)
                    .setKey(apiKey);  // API 키를 설정

            // recommend 처리: popular면 조회수 순, 아니면 기본(관련성순)
            if ("popular".equalsIgnoreCase(recommend)) {
                searchRequest.setOrder("viewCount");
            } else {
                // 기본값: 관련성순 (랜덤은 좀더 로직을 고민해봐야함!!)
                searchRequest.setOrder("relevance");
            }

            // 유튜브 API 요청을 실행하고 응답을 받음
            SearchListResponse searchResponse = searchRequest.execute();  // 유튜브 API 요청 실행
            List<SearchResult> searchResultList = searchResponse.getItems();  // 검색된 결과 리스트

            // 검색 결과가 존재하면 영상 길이 기준으로 필터링 후 리스트에 추가
            if (searchResultList != null) {
                //영상 길이 필터링 하기
                videos = filterVideosByDuration(youtubeService, searchResultList, duration);
            }

        } catch (Exception e) {
            log.error("YouTube API 호출 중 오류 발생: {}", e.getMessage(), e);
        }

        return videos;  // 검색된 비디오 제목 리스트를 반환
    }



    /**
     * 유튜브 검색 결과 중 영상 길이로 필터링하는 메서드
     * @param youtubeService YouTube API 서비스 객체
     * @param searchResults 검색 결과 리스트
     * @param duration 사용자가 선택한 길이 ("short", "medium", "long")
     * @return 필터링된 영상 리스트 (제목 + URL)
     */
    private List<String> filterVideosByDuration(YouTube youtubeService, List<SearchResult> searchResults, String duration) {
        List<String> filteredVideos = new ArrayList<>();

        try {
            // 검색 결과의 videoId만 모아서 videoId 리스트 만들기
            List<String> videoIds = new ArrayList<>();
            for (SearchResult result : searchResults) { // searchResults 여기서 가져와서 videoIds 넣기
                videoIds.add(result.getId().getVideoId());
            }

            // 비디오 상세 정보 요청 (duration 가져올 수 있는 'contentDetails' 추가)
            YouTube.Videos.List videoDetailsRequest = youtubeService.videos()
                    .list(List.of("snippet", "contentDetails"))
                    .setId(videoIds)
                    .setKey(apiKey);

            VideoListResponse videoResponse = videoDetailsRequest.execute();
            List<Video> videoDetailsList = videoResponse.getItems();

            // 비디오들의 길이를 확인하고, duration 필터에  해당하는 영상만 리스트에 추가
            for (Video video : videoDetailsList) {
                String isoDuration = video.getContentDetails().getDuration();  // ISO 8601 형식
                long seconds = parseDurationToSeconds(isoDuration);  // 영상 초 단위로 변환

                boolean isMatch = switch (duration) {
                    case "short" -> seconds >= 0 && seconds < 600;    // 5~10분 미만
                    case "medium" -> seconds >= 600 && seconds < 1800;   // 10~30분 미만
                    case "long" -> seconds >= 1800;                      // 30분 이상
                    default -> true;  // duration이 null이거나 전체일 경우 필터 없이 다 보여줌
                };

                //영상 길이 필터링 완료 됐을 때
                if (isMatch) {
                    String title = video.getSnippet().getTitle();
                    String videoId = video.getId();
                    filteredVideos.add(title + " (https://www.youtube.com/watch?v=" + videoId + ")");
                    System.out.println(title + " (https://www.youtube.com/watch?v=" + videoId  + ")" + " duration : "  + video.getContentDetails().getDuration()) ;
                }
            }

        } catch (Exception e) {
            log.error("영상 길이 필터링 중 오류 발생: {}", e.getMessage(), e);
        }

        return filteredVideos;
    }

    /**
     * ISO 8601 형식의 Duration을 초 단위로 변환하는 메서드
     * @param isoDuration ISO 8601 형식 문자열
     * @return 총 초(second) 단위 길이
     */
    private long parseDurationToSeconds(String isoDuration) {
        // java.time.Duration 이용하여 ISO 8601 Duration 파싱
        return java.time.Duration.parse(isoDuration).getSeconds();
    }
}
