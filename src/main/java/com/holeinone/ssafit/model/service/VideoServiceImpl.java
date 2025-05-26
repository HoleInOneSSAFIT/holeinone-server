package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.*;
import com.holeinone.ssafit.util.AuthUtil;
import com.holeinone.ssafit.util.S3Uploader;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.holeinone.ssafit.model.dao.VideoDao;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    private final VideoDao videoDao;

    private final String apiKey;

    private final S3Uploader s3Uploader;

    private final AuthUtil authUtil;

    long videoIdx = 0;

    // application.properties에 넣어둔 값 자바 변수로 넣어줌
    public VideoServiceImpl(VideoDao videoDao,
                            @Value("${youtube.api.key}") String apiKey,
                            S3Uploader s3Uploader, AuthUtil authUtil) {
        this.videoDao = videoDao;
        this.apiKey = apiKey;
        this.s3Uploader = s3Uploader;
        this.authUtil = authUtil;
    }

    // 유튜브 영상 검색
    //todo : 유저 완료
    @Override
    public List<YoutubeVideo> searchVideos(String part, String duration, String recommend, String token) {

        List<YoutubeVideo> videos = new ArrayList<>();  // 비디오 정보를 저장할 리스트

        log.info("유튜브 검색 키워드 : {}", part);

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
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!50
            final long MAX_RESULTS_PER_REQUEST = 20; //한 번 요청할 때 최대 몇 개의 영상을 가져올지(최대 50개)
            final int MAX_TOTAL_RESULTS = 50; //최종적으로 몇개의 영상을 가져올지

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
                    List<YoutubeVideo> filtered = filterVideosByDuration(part,youtubeService, searchResultList, duration, token);
                    videos.addAll(filtered);  // 필터링된 영상 리스트 누적 추가
                    totalResultsFetched += searchResultList.size();  // 현재까지 가져온 개수 카운트
                    log.info("현재까지 영상 개수: {}",  videos.size()); // 진행상황 출력
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
    //todo : 유저 완료
    private List<YoutubeVideo> filterVideosByDuration(String part , YouTube youtubeService, List<SearchResult> searchResults, String duration, String token) {
        List<YoutubeVideo> filteredVideos = new ArrayList<>(); // 최종 반환 리스트

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

            //유저 아이디 반환
            Long userId = authUtil.extractUserIdFromToken(token);

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
                    YoutubeVideo youtubeVideo = new YoutubeVideo();
                    youtubeVideo.setYoutubeVideoId(videoIdx++);
                    youtubeVideo.setSourceType("RECOMMENDED");
                    youtubeVideo.setVideoUrl("https://www.youtube.com/watch?v=" + videoId);
                    youtubeVideo.setTitle(title);
                    youtubeVideo.setDurationSeconds((int) seconds);
                    youtubeVideo.setChannelName(video.getSnippet().getChannelTitle());
                    youtubeVideo.setPart(part); //운동 부위 저장
                    youtubeVideo.setCreatedAt(null); // 필요 시 DB 저장 시점에 설정
                    youtubeVideo.setUpdatedAt(null);
                    youtubeVideo.setUserId(userId); // 유튜브 영상은 사용자 업로드가 아니므로 null

                    filteredVideos.add(youtubeVideo);
                    log.info("videoId = {}, duration = {}", videoId, isoDuration);
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

    //루틴 운동 영상 저장하기
    //todo : 유저
    @Transactional
    @Override
    public Long insertVideoRoutine(List<YoutubeVideo> youtubeVideoList,
                                   List<UploadedVideo> uploadedVideoList,
                                   String routineTitle, String routineContent,
                                    String token) {
        try {
            int result = 0;

            //유저 아이디 반환
            Long userId = authUtil.extractUserIdFromToken(token);

            // 루틴 객체 생성 및 insert
            Routine routine = new Routine();
            routine.setIsShared(false); // 기본 비공유
            routine.setRoutineTitle(routineTitle); //루틴 제목
            routine.setRoutineContent(routineContent); //루틴 내용
            routine.setUserId(userId); //유저 아이디

            log.info("유저 아이디 {} ", routine.getUserId());

            //1. 운동 루틴 생성 -> 아이디 반환
            videoDao.createRoutine(routine);
            long routineId = routine.getRoutineId(); // 루틴 ID가 있음

            // 루틴 ID가 할당되지 않은 경우
            if (routineId == 0) {
                throw new IllegalStateException("루틴 생성 실패: routineId가 0입니다.");
            }

            log.info("루틴 아이디 {} ", routineId);

            //2. 유튜브 영상 저장(유튜브 영상이 있다면)
            if(youtubeVideoList != null) {
                for (YoutubeVideo youtubeVideo : youtubeVideoList) {
                    youtubeVideo.setUserId(userId); //유저 아이디 저장
                    // YoutubeVideo 저장
                    videoDao.insertVideoRoutine(youtubeVideo);

                    RoutineVideo rv = new RoutineVideo();
                    rv.setSequenceOrder(youtubeVideo.getYoutubeSequence()); //영상 순서 저장
                    rv.setRoutineId(routineId); //루틴 아이디 저장
                    rv.setYoutubeVideoId(youtubeVideo.getYoutubeVideoId()); //유튜브 아이디 저장

                    //영상-루틴 매핑 저장
                    videoDao.insertRoutineYoutubeVideo(rv);
                }
            }

            // 3. 업로드 영상 저장
            if (uploadedVideoList != null) {
                for (UploadedVideo uploadedVideo : uploadedVideoList) {
                    uploadedVideo.setUserId(userId); //유저 아이디 저장
                    // UploadedVideo 저장
                    videoDao.insertUploadedRoutine(uploadedVideo);

                    RoutineVideo rv = new RoutineVideo();
                    rv.setSequenceOrder(uploadedVideo.getUploadedSequence()); //영상 순서 저장
                    rv.setRoutineId(routineId); //루틴 아이디 저장
                    rv.setUploadedVideoId(uploadedVideo.getUploadedVideoId()); //업로드 영상 아이디 저장

                    videoDao.insertRoutineUploadedVideo(rv);
                }
            }

            return routineId;
        } catch (Exception e) {
            log.error("루틴 저장 중 오류 발생", e);
            throw new RuntimeException("루틴 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    //영상 직접 업로드
    @Override
    //todo: 유저
    public UploadedVideo uploadVideo(MultipartFile file, UploadedVideo uploadedVideo, String token) {

        try {

            //유저 아이디 반환
            Long userId = authUtil.extractUserIdFromToken(token);

            //S3 업로드
            String videoUrl = s3Uploader.upload(file, "uploaded-videos");

            uploadedVideo.setVideoUrl(videoUrl); //비디오 url 저장
            uploadedVideo.setOriginalFilename(file.getOriginalFilename()); //파일 기존 이름
            uploadedVideo.setUserId(userId); //유저 아이디 저장

            //영상 저장(s3 업로드, db저장 x)
            return uploadedVideo;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //내가 올린 유튜브 영상
    //todo: 유저 완료!
    @Override
    public YoutubeVideo directYoutubeUrl(String url, String part, int sequence, int restSecondsAfter, String token) {
        //1. 유튜브 url에서 아이디 추출
        String videoId = extractVideoId(url);

        //url이 없으면 유효하지 않다는 메시지 리턴
        if (videoId == null) {
            throw new IllegalArgumentException("유효하지 않은 YouTube URL입니다.");
        }

        //2. YouTube API로 videoId 기반 영상 정보 조회
        try {
            // YouTube API 서비스 객체 생성
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("holeinone-ssafit")
                    .build();

            // videoId로 영상 상세 조회 요청 (snippet, contentDetails 포함)
            YouTube.Videos.List videosList = youtubeService.videos()
                    .list(List.of("snippet", "contentDetails"))
                    .setId(List.of(videoId))
                    .setKey(apiKey);

            VideoListResponse videoResponse = videosList.execute();
            List<Video> items = videoResponse.getItems();

            if (items == null || items.isEmpty()) {
                throw new IllegalStateException("해당 영상 정보를 찾을 수 없습니다.");
            }

            Video video = items.get(0); //영상 리스트 중 첫 번째 영상 객체 꺼내기

            // 영상 길이 초 단위 변환
            long seconds = parseDurationToSeconds(video.getContentDetails().getDuration());

            //유저 아이디 반환
            Long userId = authUtil.extractUserIdFromToken(token);

            // YoutubeVideo DTO 생성
            YoutubeVideo youtubeVideo = new YoutubeVideo();
            youtubeVideo.setSourceType("MY_UPLOAD");
            youtubeVideo.setVideoUrl(url);
            youtubeVideo.setTitle(video.getSnippet().getTitle());
            youtubeVideo.setDurationSeconds((int) seconds);
            youtubeVideo.setChannelName(video.getSnippet().getChannelTitle());
            youtubeVideo.setRestSecondsAfter(restSecondsAfter);
            youtubeVideo.setPart(part);
            youtubeVideo.setYoutubeSequence(sequence);
            youtubeVideo.setUserId(userId);

            //유튜브 ID 값 반환 받기
//        int youtubeVideoResultId = videoDao.insertVideoRoutine(youtubeVideo);
//        log.info("유튜브 ID : {} ", youtubeVideo.getYoutubeVideoId());
//
//        //저장한 유튜브 객체 반환(아이디를 통해 조회)
//        YoutubeVideo savedVideo = videoDao.selectYoutubeVideoById(Math.toIntExact(youtubeVideo.getYoutubeVideoId()));

            //저장한 영상 리턴
            return youtubeVideo;

        } catch (IOException | GeneralSecurityException e) {
            log.error("YouTube API 통신 오류: {}", e.getMessage(), e);
            throw new RuntimeException("YouTube API 통신 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("YouTube 영상 조회 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("서버 오류가 발생했습니다.");
        }
    }

    //루틴 아이디를 통해 루틴 삭제
    @Transactional
    @Override
    public int routineIdDelete(long routineId) {
        try {
            // 1. S3 파일 URL 수집 및 삭제
            List<String> routineFileUrls = videoDao.routineFileUrl(routineId);
            List<String> postFileUrls = videoDao.postFileUrl(routineId);

            List<String> allFileUrls = new ArrayList<>();
            allFileUrls.addAll(routineFileUrls);
            allFileUrls.addAll(postFileUrls);

            for (String fileUrl : allFileUrls) {
                s3Uploader.delete(fileUrl);
            }

            // 2. 루틴 관련 데이터 삭제 순서
            // (루틴-영상 매핑 테이블에서 영상 ID 먼저 가져오기)
            List<RoutineVideoId> videoIds = videoDao.routineVideoIds(routineId);

            for(RoutineVideoId videoId : videoIds) {
                log.info("비디오 아이디 : {} ", videoId);
            }

            // 루틴-영상 매핑 테이블, 루틴, 게시글, 게시글 파일 등 한꺼번에 삭제
            int deletedResult = videoDao.routineIdDelete(routineId);

            if (deletedResult == 0) {
                throw new RuntimeException("루틴 삭제 실패: 존재하지 않는 ID");
            }

            // 3. 영상 테이블 삭제
            for (RoutineVideoId idPair : videoIds) {
                if (idPair.getYoutubeVideoId() != null) {
                    videoDao.deleteYoutubeById(idPair.getYoutubeVideoId());
                }
                if (idPair.getUploadedVideoId() != null) {
                    videoDao.deleteUploadedById(idPair.getUploadedVideoId());
                }
            }

            return deletedResult;
        } catch (Exception e) {
            log.error("루틴 삭제 중 오류 발생", e);
            throw new RuntimeException("루틴 삭제 실패", e);
        }
    }

    //s3에 올라온 임시 루틴 영상 삭제하기
    @Override
    public boolean tempUploadRoutineDelete(String uploadUrl) {

        //파일에서 삭제
        return s3Uploader.delete(uploadUrl);
    }

    //유튜브 url 아이디 추출
    private String extractVideoId(String url) {

        try {
            //1. 짧은 공유 URL 형식
            if (url.contains("youtu.be/")) {
                // https://youtu.be/VIDEO_ID, 마지막 '/' 이후의 문자열이 videoId
                return url.substring(url.lastIndexOf("/") + 1);
            //2. 표준 YouTube URL 형식
            } else if (url.contains("watch?v=")) {
                // https://www.youtube.com/watch?v=VIDEO_ID, , URL에서 쿼리 파라미터 부분만 추출 (? 이후)
                String query = new URL(url).getQuery(); //쿼리 문자열 부분만 반환
                for (String param : query.split("&")) { //여러개의 & 파라미터 나눔
                    String[] pair = param.split("="); //= 기준으로 또 나눔
                    if (pair[0].equals("v")) return pair[1]; //쪼갠 파라미터의 이름이 v면 파라미터의 값을 가져오기
                }
            //3. Shorts 영상 URL 형식
            } else if (url.contains("/shorts/")) {
                // https://www.youtube.com/shorts/VIDEO_ID, 마지막 '/' 이후의 문자열이 videoId
                return url.substring(url.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            return null;
        }
        return null;

    }



}
