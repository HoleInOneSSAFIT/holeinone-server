package com.holeinone.ssafit.model.service;

import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
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

    //application.properties에 넣어둔 값 자바 변수로 넣어줌
    public VideoServiceImpl(@Value("${youtube.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    //유튜브 영상 검색
    @Override
    public List<String> searchVideos(String searchQuery, String duration, String recommend) {

        List<String> videos = new ArrayList<>();  // 비디오 정보를 저장할 리스트

        try {
            // YouTube API 서비스 객체를 생성 (Google API와 연결)
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),  // Google API 서버와 통신할 HTTP 연결
                    GsonFactory.getDefaultInstance(),  //  응답 JSON 데이터를 파싱할 GsonFactory
                    null  // 인증이 필요하지 않음 (API 키로 인증)
            ).setApplicationName("holeinone-ssafit").build();  // Google API에 애플리케이션 이름 명시, YouTube 서비스 객체 생성

            // 유튜브 검색 요청을 생성
            YouTube.Search.List searchRequest = youtubeService.search()
                    .list(List.of("snippet"))  // 검색할 데이터 종류 설정 ('snippet'은 제목, 설명 등 기본 정보)
                    .setQ(searchQuery)  // searchQuery 값으로 검색
                    .setType(List.of("video"))  // 검색 결과 중 비디오만 필터링
                    .setMaxResults(5L)  // 최대 5개의 결과만 가져오기
                    .setKey(apiKey);  // API 키를 설정

            // recommend 처리: popular면 조회수 순, 아니면 기본(관련성순)
            if ("popular".equalsIgnoreCase(recommend)) {
                searchRequest.setOrder("viewCount");
            } else  {
                // 기본값: 관련성순 (랜덤은 좀더 로직을 고민해봐야함!!)
                searchRequest.setOrder("relevance");
            }

            // 유튜브 API 요청을 실행하고 응답을 받음
            SearchListResponse searchResponse = searchRequest.execute(); //유튜브 API 요청 실행
            List<SearchResult> searchResultList = searchResponse.getItems();  // 검색된 결과 리스트



            // 검색 결과가 존재하면 각 비디오의 제목과 URL을 리스트에 추가
            if (searchResultList != null) {
                //SearchResult는 YouTube API의 검색 결과 표현하는 객체
                for (SearchResult searchResult : searchResultList) {
                    String title = searchResult.getSnippet().getTitle();  // 비디오 제목
                    String videoId = searchResult.getId().getVideoId();  // 비디오 ID
                    // 제목과 URL을 결합하여 리스트에 추가
                    videos.add(title + " (https://www.youtube.com/watch?v=" + videoId + ")");
                    System.out.println(title + " (https://www.youtube.com/watch?v=" + videoId + ")");
                }
            }
        } catch (Exception e) {
            log.error("YouTube API 호출 중 오류 발생: {}", e.getMessage(), e);
        }

        return videos;  // 검색된 비디오 제목 리스트를 반환

    }
}
