package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;
import com.holeinone.ssafit.model.dto.YoutubeVideo;
import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.VideoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

//final 또는 @NonNull이 붙은 필드를 모두 파라미터로 받는 생성자 자동 생성
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoService videoService;
    private final PostService postService;


    /**
     * 지정된 쿼리 매개변수와 일치하는 검색 결과 컬렉션을 반환*
     *
     * @param part 운동 부위(특정 or 전체), duration 영상 길이(특정 or 전체), recommend 추천 방식(필수)
     * @return 영상 제목 + 링크 목록
     */
    //todo : 유저 완료
    @GetMapping("/search")
    public ResponseEntity<?> searchYoutubeVideos(@RequestParam String part,
                                                 @RequestParam(required = false, defaultValue = "") String duration,
                                                 @RequestParam(required = false, defaultValue = "") String recommend,
                                                 HttpSession session, @RequestHeader("Authorization") String token) {
        try {
            String searchQuery = "운동";

            // part가 "전체"이면 운동만 서치
            if ("전체".equalsIgnoreCase(part)) {
                part = searchQuery;
            } else { //정해진 부위가 있다면 부위 + "운동"
                part = part + searchQuery;
            }

            // duration도 "전체"이면 빈 문자열 처리
            if ("전체".equalsIgnoreCase(duration)) {
                duration = "";
            }

            //랜덤 영상 리스트(만약 사용자가 내가 뽑아낸 영상을 다 넘겼다면? 어떻게 해야할지 생각이 필요하다)
            //todo: 재추천 로직이 필요한것으로 생각됩니다(기존 리스트랑 겹치지 않게)
            List<YoutubeVideo> videos = videoService.searchVideos(part, duration, recommend, token);

            if (videos.isEmpty()) { //만약 영상이 없다면 오류 상태 던지기
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 조건에 맞는 영상이 없습니다.");
            }

            // VideoRoutineSessionData 객체 생성 및 세팅
            VideoRoutineSessionData videoRoutineData = new VideoRoutineSessionData();
            videoRoutineData.setYoutubeVideoList(videos);

            session.setAttribute("videoRoutineData", videoRoutineData); //세션에 영상 리스트 저장하기

            VideoRoutineSessionData result = (VideoRoutineSessionData) session.getAttribute("videoRoutineData");

            log.info("랜덤 영상 리스트 : {}, 사이즈 : {} ", result.getYoutubeVideoList(), result.getYoutubeVideoList().size());

            //여러개의 영상을 랜덤으로 하나 픽하기
            int randomIdx = new Random().nextInt(videos.size());
            YoutubeVideo video = videos.get(randomIdx);

            //뽑은 영상 리스트 중에 하나 뽑아서 프론트로 던지기
            return ResponseEntity.ok(video);

        } catch (Exception e) {
            log.error("유튜브 영상 검색 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유튜브 영상 검색 중 서버 오류가 발생했습니다.");
        }
    }

    /**
     * @videoId 스킵하고자 하는 영상의 아이디
     **/
    @GetMapping("/reSearch")
    public ResponseEntity<?> reSearchYoutubeVideos(@RequestParam long youtubeVideoId, HttpSession session) {

        log.info("다음 영상 추천 받기");

        //세션에 담은 랜덤 유튜브 영상 list 꺼내오기
        VideoRoutineSessionData videoList = (VideoRoutineSessionData) session.getAttribute("videoRoutineData");

        if (videoList == null || videoList.getYoutubeVideoList().isEmpty()) { //저장된 영상 리스트가 없을 때
            log.warn("세션에 저장된 유튜브 영상 리스트가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("세션에 저장된 영상 리스트가 없습니다. 다시 검색해 주세요.");
        }

        // videoId에 해당하는 영상 제거
        videoList.getYoutubeVideoList().removeIf(video -> video.getYoutubeVideoId() == youtubeVideoId);

        log.info("남은 영상 리스트 : {}", videoList.getYoutubeVideoList().size());

        if (videoList.getYoutubeVideoList().isEmpty()) {
            //추천할 영상이 더이상 없을 때
            //todo: 프론트에서 다시 /search 요청 보내도록 해야함 🖥️
            log.warn("모든 추천 영상을 소진했습니다. videoId: {}", youtubeVideoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("추천할 영상이 더 이상 없습니다. 조건을 바꿔 다시 검색해 주세요.");
        }

        // 남은 리스트 중 랜덤 추천
        int randomIdx = new Random().nextInt(videoList.getYoutubeVideoList().size());
        YoutubeVideo nextVideo = videoList.getYoutubeVideoList().get(randomIdx);

        // 변경된 랜덤 유튜브 리스트 다시 세션에 저장
        session.setAttribute("videoRoutineData", videoList);

        //다음 랜덤 유튜브 영상 추천하기
        return ResponseEntity.ok(nextVideo);
    }

    //랜덤으로 담은 유튜브 영상 중 하나를 선택하고자 할 때
    @GetMapping("/youtubeSelect")
    public ResponseEntity<?> youtubeSelect(@RequestParam long youtubeVideoId,
                                           @RequestParam int sequence,
                                           @RequestParam int restSecondsAfter,
                                           HttpSession session) {

        log.info("유튜브 영상 선택");

        // 유튜브 랜덤 영상 저장소
        VideoRoutineSessionData videoRoutineData = (VideoRoutineSessionData) session.getAttribute("videoRoutineData");

        // 운동 루틴 영상들 임시 저장소 (null일 경우 새 객체 생성)
        VideoRoutineSessionData videoRoutineResult = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");
        if (videoRoutineResult == null) {
            videoRoutineResult = new VideoRoutineSessionData();  // 새 객체 생성
        }

        // youtube 리스트가 null일 경우 초기화
        if (videoRoutineResult.getYoutubeVideoList() == null) {
            videoRoutineResult.setYoutubeVideoList(new ArrayList<>());
        }

        YoutubeVideo selectedVideo = null;

        // 내가 선택한 영상이 videoRoutineData 리스트 안에 있다면 루틴에 추가
        if (videoRoutineData != null && videoRoutineData.getYoutubeVideoList() != null) {
            for (YoutubeVideo video : videoRoutineData.getYoutubeVideoList()) {
                if (video.getYoutubeVideoId() == youtubeVideoId) {
                    video.setYoutubeSequence(sequence); // 루틴 순서 지정
                    video.setRestSecondsAfter(restSecondsAfter); //운동 후 쉬는 시간 지정
                    videoRoutineResult.getYoutubeVideoList().add(video); // 루틴 리스트에 추가
                    selectedVideo = video;
                    break;
                }
            }
        }

        // 세션에 루틴 결과 저장
        session.setAttribute("videoRoutineResult", videoRoutineResult);
        session.removeAttribute("videoRoutineData"); // 랜덤 영상 저장소 초기화

        if (selectedVideo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 유튜브 영상을 찾을 수 없습니다.");
        }

        log.info("루틴 영상 리스트 : {}, 사이즈 : {} ",
                videoRoutineResult,
                (videoRoutineResult.getYoutubeVideoList().size() +
                        (videoRoutineResult.getUploadVideoList() == null ? 0 : videoRoutineResult.getUploadVideoList().size())));

        return ResponseEntity.ok(selectedVideo); // 선택된 영상 반환
    }

    //운동 루틴 하나씩 조회
    @GetMapping("/routineSelect/{sequence}")
    public ResponseEntity<?> routineSelect(@PathVariable int sequence, HttpSession session) {

        // 세션에서 루틴 데이터 꺼내기
        VideoRoutineSessionData routineData = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        //세션에 루틴 데이터 자체가 없는 경우
        if (routineData == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "루틴 정보가 없습니다.");
        }

        //유튜브 영상, 업로드 영상
        List<YoutubeVideo> youtubeVideoList = routineData.getYoutubeVideoList();
        List<UploadedVideo> uploadedVideoList = routineData.getUploadVideoList();

        // 루틴은 있지만 안에 아무 영상도 담겨있지 않는 경우
        if ((youtubeVideoList == null || youtubeVideoList.isEmpty()) &&
                (uploadedVideoList == null || uploadedVideoList.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회할 영상이 없습니다.");
        }

        //조회할 영상이 유튜브 영상중에 있다면 해당 영상 정보 리턴
        if(youtubeVideoList != null) {
            for (YoutubeVideo video : youtubeVideoList) {
                if (video.getYoutubeSequence() == sequence) {
                    return ResponseEntity.ok(video);
                }
            }
        }
        //조회할 영상이 업로드 영상중에 있다면 해당 영상 정보 리턴
        if(uploadedVideoList != null) {
            for (UploadedVideo video : uploadedVideoList) {
                if(video.getUploadedSequence() == sequence) {
                    return ResponseEntity.ok(video);
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 순서의 영상이 없습니다.");
    }

    //운동 루틴 하나 수정


    //운동 루틴 중 하나 삭제
    @GetMapping("/routineDelete/{sequence}")
    public ResponseEntity<?>  routineDelete(@PathVariable int sequence, HttpSession session) {

        // 세션에서 루틴 데이터 꺼내기
        VideoRoutineSessionData routineData = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        //세션에 루틴 데이터 자체가 없는 경우
        if (routineData == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "루틴 정보가 없습니다.");
        }

        //유튜브 영상, 업로드 영상
        List<YoutubeVideo> youtubeVideoList = routineData.getYoutubeVideoList();
        List<UploadedVideo> uploadedVideoList = routineData.getUploadVideoList();

        // 루틴은 있지만 안에 아무 영상도 담겨있지 않는 경우
        if ((youtubeVideoList == null || youtubeVideoList.isEmpty()) &&
                (uploadedVideoList == null || uploadedVideoList.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제할 영상이 없습니다.");
        }

        // 유튜브 영상 삭제 및 순서 정렬
        if (youtubeVideoList != null) {
            // 삭제할 순서의 루틴 영상인 경우 삭제
            youtubeVideoList.removeIf(video -> video.getYoutubeSequence() == sequence);
            // 삭제 순서 이후의 영상들은 순서 -1 조정
            for (YoutubeVideo video : youtubeVideoList) {
                if (video.getYoutubeSequence() > sequence) {
                    video.setYoutubeSequence(video.getYoutubeSequence() - 1);
                }
            }
        }

        // 업로드 영상 삭제 및 순서 정렬
        if (uploadedVideoList != null) {
            // 먼저 삭제할 video 찾기
            UploadedVideo target = null;
            for (UploadedVideo video : uploadedVideoList) {
                if (video.getUploadedSequence() == sequence) {
                    target = video;
                    break;
                }
            }

            // s3에서 삭제
            if (target != null) {
                boolean result = videoService.tempUploadRoutineDelete(target.getVideoUrl());
                uploadedVideoList.remove(target);
            }

            // 나머지 영상들 순서 정렬
            for (UploadedVideo video : uploadedVideoList) {
                if (video.getUploadedSequence() > sequence) {
                    video.setUploadedSequence(video.getUploadedSequence() - 1);
                }
            }
        }

        // 세션에 반영
        session.setAttribute("videoRoutineResult", routineData);

        return ResponseEntity.ok(routineData);
    }


    //세션에 담긴 임시 루틴 전부 초기화
    @GetMapping("/tempRoutineReset")
    public ResponseEntity<?>  tempRoutineReset(HttpSession session) {

        //세션에 담긴 임시 루틴 영상 가져오기
        VideoRoutineSessionData videoRoutineResultList = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        //VideoRoutineSessionData 객체 내부 초기화
        if (videoRoutineResultList != null) {
            if (videoRoutineResultList.getYoutubeVideoList() != null) {
                log.info("초기화 전 - 유튜브 리스트 사이즈: {}", videoRoutineResultList.getYoutubeVideoList().size());
                videoRoutineResultList.getYoutubeVideoList().clear();
            }
            if (videoRoutineResultList.getUploadVideoList() != null) {
                log.info("초기화 전 - 업로드 리스트 사이즈: {}", videoRoutineResultList.getUploadVideoList().size());
                videoRoutineResultList.getUploadVideoList().clear();
            }
        }

        // 세션에서도 제거
        session.removeAttribute("videoRoutineResult");

        log.info("videoRoutineResultList 객체 내부 확인 : {}", videoRoutineResultList);
        log.info("세션 내부 확인 : {}", session.getAttribute("videoRoutineResult"));

        return ResponseEntity.ok("임시 루틴과 내부 리스트가 초기화되었습니다.");

    }


    /**
     * Videos 화면에서 받아온 비디오 값 저장하여 전달
     **/
    //루틴에 영상들 저장하기
    //todo : 유저
    @PostMapping("/insertVideoRoutine")
    public ResponseEntity<?> insertVideo(HttpSession session, @RequestParam String routineTitle,
                                         @RequestParam String routineContent, @RequestHeader("Authorization") String token) {

        //운동 루틴 영상들 임시 저장소
        VideoRoutineSessionData routineData = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        //유튜브 리스트와 업로드 리스트 꺼내오기, 없다면 null로 유지
        List<YoutubeVideo> youtubeVideoList = routineData != null ? routineData.getYoutubeVideoList() : null;
        List<UploadedVideo> uploadedVideoList = routineData != null ? routineData.getUploadVideoList() : null;

        //리스트에 둘다 아무것도 없다면 오류 메시지 던지기
        if ((youtubeVideoList == null || youtubeVideoList.isEmpty()) &&
                (uploadedVideoList == null || uploadedVideoList.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "저장할 영상이 없습니다");
        }

        // 유튜브 영상 ID 초기화(그 전에 랜덤으로 영상 뽑으면서 임시로 랜덤으로 아이디를 지정해뒀음)
        if (youtubeVideoList != null) {
            for (YoutubeVideo youtubeVideo : youtubeVideoList) {
                youtubeVideo.setYoutubeVideoId(0L);
            }
        }

        //운동 루틴 저장하기
        Long routineId  = videoService.insertVideoRoutine(
                youtubeVideoList != null ? youtubeVideoList : Collections.emptyList(),
                uploadedVideoList != null ? uploadedVideoList : Collections.emptyList(),
                routineTitle, routineContent, token
        );

        //루틴 영상 세션 초기화
        session.removeAttribute("videoRoutineResult");

        //루틴 아이디를 통해 루틴 영상 정보 조회
        List<VideoRoutineSessionData> routineVideoList = postService.getRoutineById(routineId);

        // 루틴 ID, 메시지, 루틴 영상 리스트를 함께 리턴
        Map<String, Object> response = new HashMap<>();
        response.put("routineId", routineId);
        response.put("message", "루틴이 성공적으로 저장되었습니다");
        response.put("routineVideos", routineVideoList);

        log.info("출력 될 영상 정보 : {}", routineVideoList);

        //영상 루틴 출력
        return ResponseEntity.ok(response);
    }

    //루틴 삭제
    @DeleteMapping("/routineIdDelete/{routineId}")
    public ResponseEntity<?>  routineIdDelete(@PathVariable long routineId) {

        try {
            //루틴 아이디를 통해 루틴 삭제
            int result = videoService.routineIdDelete(routineId);
            return ResponseEntity.ok("삭제 성공: " + result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("루틴 삭제 실패: " + e.getMessage());
        }

    }


    /***
     * @file 내가 업로드한 영상 파일
     * @UploadedVideo 그 외 영상 정보
     * */
    //내가 찍은 영상 올리기
    //todo: 유저
    @PostMapping("/myUpload")
    public ResponseEntity<UploadedVideo> uploadVideo(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("title") String title,
                                                     @RequestParam("part") String part,
                                                     @RequestParam("durationSeconds") int durationSeconds,
                                                     @RequestParam("sequence") int sequence,
                                                     @RequestParam("restSecondsAfter") int restSecondsAfter,
                                                     @RequestHeader("Authorization") String token,
                                                     HttpSession session) {

        UploadedVideo uploadedVideo = new UploadedVideo();
        uploadedVideo.setTitle(title);
        uploadedVideo.setPart(part);
        uploadedVideo.setDurationSeconds(durationSeconds);
        uploadedVideo.setUploadedSequence(sequence);
        log.info("운동 후 쉬는 시간 : {}", restSecondsAfter);
        uploadedVideo.setRestSecondsAfter(restSecondsAfter);

        //영상 S3에 저장하러가기
        UploadedVideo videoDTO = videoService.uploadVideo(file, uploadedVideo, token);

        // 세션에서 리스트 꺼내기
        VideoRoutineSessionData uploadVideoList = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        // 비어있으면 새 리스트 생성
        if (uploadVideoList == null) {
            uploadVideoList = new VideoRoutineSessionData();
        }

        // 새 업로드된 영상 추가
        uploadVideoList.getUploadVideoList().add(videoDTO);

        // 세션에 다시 저장
        session.setAttribute("videoRoutineResult", uploadVideoList);

        VideoRoutineSessionData result = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        log.info("루틴 영상 리스트 : {}, 사이즈 : {} ", result, (result.getYoutubeVideoList().size() + result.getUploadVideoList().size()));

        log.info("나의 영상 정보가 넘어갔는지 확인 {}", result);

        return ResponseEntity.ok(videoDTO);
    }


    //내가 유튜브 url 직접 입력
    //todo: 유저 완료!
    @PostMapping("/directYoutubeUrl")
    public ResponseEntity<?> directYoutubeUrl(@RequestParam("url") String url,
                                              @RequestParam("part") String part,
                                              @RequestParam("sequence") int sequence,
                                              @RequestParam("restSecondsAfter") int restSecondsAfter,
                                              HttpSession session,
                                              @RequestHeader("AUthorization") String token) {

        System.out.println(restSecondsAfter);

        //내가 올린 유튜브 url
        YoutubeVideo directYoutubeVideo;
        try {
            directYoutubeVideo = videoService.directYoutubeUrl(url, part, sequence, restSecondsAfter, token);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 잘못된 요청에 대한 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 서버 에러
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }

        // 세션에서 리스트 꺼내기
        VideoRoutineSessionData videoList = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        // 비어있으면 새 리스트 생성
        if (videoList == null) {
            videoList = new VideoRoutineSessionData();
        }

        videoList.getYoutubeVideoList().add(directYoutubeVideo);

        // 세션에 다시 저장
        session.setAttribute("videoRoutineResult", videoList);

        VideoRoutineSessionData result = (VideoRoutineSessionData) session.getAttribute("videoRoutineResult");

        log.info("루틴 영상 리스트 : {}, 사이즈 : {} ", result, (result.getYoutubeVideoList().size() + result.getUploadVideoList().size()));

        log.info("내가 가져온 유튜브 영상 {}", directYoutubeVideo);

        //저장한 유튜브 url 반환
        return ResponseEntity.ok(directYoutubeVideo);
    }
}