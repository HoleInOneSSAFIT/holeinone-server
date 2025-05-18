package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.Videos;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    private final VideoDao videoDao;

    private final String apiKey;

    long videoIdx = 0;

    // application.properties에 넣어둔 값 자바 변수로 넣어줌
    public VideoServiceImpl(VideoDao videoDao, @Value("${youtube.api.key}") String apiKey) {
        this.videoDao = videoDao;
        this.apiKey = apiKey;
    }

    // 유튜브 영상 검색
    @Override
    public List<Videos> searchVideos(String part, String duration, String recommend) {

        List<Videos> videos = new ArrayList<>();  // 비디오 정보를 저장할 리스트

        System.out.println(part);

        try {
            // YouTube API 서비스 객체를 생성 (Google API와 연결)
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),  // Google API 서버와 통신할 HTTP 연결
                    GsonFactory.getDefaultInstance(),  // 응답 JSON 데이터를 파싱할 GsonFactory
                    null  // 인증이 필요하지 않음 (API 키로 인증)
            ).setApplicationName("holeinone-ssafit").build();  // Google API에 애플리케이션 이름 명시, YouTube 서비스 객체 생성

            // 페이징 처리를 위한 변수들
            String nextPageToken = null; //다음 페이지 토큰 저장용 변수, YouTube API는 검색 페이지를 여러 페이지로 나눠서 줌
            int totalResultsFetched = 0; //지금까지 몇 개의 영상을 가져왔는지 카운팅
            final long MAX_RESULTS_PER_REQUEST = 50; //한 번 요청할 때 최대 몇 개의 영상을 가져올지(최대 50개)
            final int MAX_TOTAL_RESULTS = 200; //최종적으로 몇개의 영상을 가져올지

            // 페이징을 위한 do-while 루프
            do {
                // 유튜브 검색 요청을 생성
                YouTube.Search.List searchRequest = youtubeService.search()
                        .list(List.of("snippet"))  // 검색할 데이터 종류 설정 ('snippet'은 제목, 설명 등 기본 정보)
                        .setQ(part)  // part 값으로 검색
                        .setType(List.of("video"))  // 검색 결과 중 비디오만 필터링
                        .setMaxResults(MAX_RESULTS_PER_REQUEST)  // 최대 50개의 결과 가져오기 (필터링 때문에 여유있게 가져옴)
                        .setKey(apiKey)  // API 키를 설정
                        .setRegionCode("KR")  // 한국 지역 필터
                        .setRelevanceLanguage("ko");  // 한국어 우선 검색

                // recommend 처리: popular면 조회수 순, 아니면 기본(관련성순)
                if ("popular".equalsIgnoreCase(recommend)) {
                    searchRequest.setOrder("viewCount");
                } else {
                    // 기본값: 관련성순 (랜덤은 좀더 로직을 고민해봐야함!!)
                    searchRequest.setOrder("relevance");
                }

                // nextPageToken 있으면 설정
                //50개를 요청했는데 결과 영상이 50개 이상이라면 nextPageToken 요청을 통해 다음 페이지 불러옴
                //첫 요청에는 nextPageToken이 없으므로 그 다음 요청이 있을때부터 nextPageToken 요청
                if (nextPageToken != null) {
                    searchRequest.setPageToken(nextPageToken);
                }

                // 유튜브 API 요청을 실행하고 응답을 받음
                SearchListResponse searchResponse = searchRequest.execute();  // 유튜브 API 요청 실행
                List<SearchResult> searchResultList = searchResponse.getItems();  // 검색된 결과 리스트

                // 검색 결과가 존재하면 영상 길이 기준으로 필터링 후 리스트에 추가
                if (searchResultList != null) {
                    //영상 길이 필터링 하기
                    List<Videos> filtered = filterVideosByDuration(youtubeService, searchResultList, duration);
                    videos.addAll(filtered);  // 필터링된 영상 리스트 누적 추가
                    totalResultsFetched += searchResultList.size();  // 현재까지 가져온 개수 카운트
                    System.out.println("현재까지 영상 개수: " + videos.size());  // 진행상황 출력
                }

                // 다음 페이지 토큰 만들어줌
                nextPageToken = searchResponse.getNextPageToken();

            } while (nextPageToken != null && totalResultsFetched < MAX_TOTAL_RESULTS);  // 페이징 종료 조건

        } catch (Exception e) {
            log.error("YouTube API 호출 중 오류 발생: {}", e.getMessage(), e);
        }
        
        //랜덤으로 추출한 영상을 db에 저장
        return videos;
    }


    /**
     * 유튜브 검색 결과 중 영상 길이로 필터링하는 메서드
     * @param youtubeService YouTube API 서비스 객체
     * @param searchResults 검색 결과 리스트
     * @param duration 사용자가 선택한 길이 ("short", "medium", "long")
     * @return 필터링된 영상 리스트 (제목 + URL)
     */
    private List<Videos> filterVideosByDuration(YouTube youtubeService, List<SearchResult> searchResults, String duration) {
        List<Videos> filteredVideos = new ArrayList<>(); // 최종 반환 리스트

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

            // 비디오들의 길이를 확인하고, duration 필터에 해당하는 영상만 리스트에 추가
            for (Video video : videoDetailsList) {
                String isoDuration = video.getContentDetails().getDuration();  // ISO 8601 형식
                long seconds = parseDurationToSeconds(isoDuration);  // 영상 초 단위로 변환

                boolean isMatch = switch (duration) {
                    case "short" -> seconds >= 0 && seconds < 600;    // 5~10분 미만
                    case "medium" -> seconds >= 600 && seconds < 1200;   // 10~20분 미만
                    case "long" -> seconds >= 1200;                      // 20분 이상
                    default -> true;  // duration이 null이거나 전체일 경우 필터 없이 다 보여줌
                };

                // 영상 길이 필터링 완료 됐을 때
                if (isMatch) {

                    String title = video.getSnippet().getTitle();
                    String videoId = video.getId();

                    // Videos 객체 생성 및 설정
                    Videos videos = new Videos();
                    videos.setVideoId(videoIdx++);
                    videos.setSourceType("YOUTUBE");
                    videos.setVideoUrl("https://www.youtube.com/watch?v=" + videoId);
                    videos.setTitle(title);
                    videos.setDurationSeconds((int) seconds);
                    videos.setChannelName(video.getSnippet().getChannelTitle());
                    videos.setCreatedAt(null); // 필요 시 DB 저장 시점에 설정
                    videos.setUpdatedAt(null);
                    videos.setUserId(null); // 유튜브 영상은 사용자 업로드가 아니므로 null

                    filteredVideos.add(videos);

                    System.out.println(title + " (https://www.youtube.com/watch?v=" + videoId  + ")" + " duration : "  + isoDuration);
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
     * @return 총 초단위 길이
     */
    private long parseDurationToSeconds(String isoDuration) {
        // java.time.Duration 이용하여 ISO 8601 Duration 파싱
        return java.time.Duration.parse(isoDuration).getSeconds();
    }

    //루틴에 영상 저장하기
    @Override
    public int insertVideoRoutine(Videos video) {

        int result = videoDao.insertVideoRoutine(video);

        return result;

    }

}
