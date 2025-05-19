package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineRequest;
import com.holeinone.ssafit.model.dto.YoutubeVideo;
import com.holeinone.ssafit.model.service.VideoService;
import com.holeinone.ssafit.util.S3Uploader;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//final 또는 @NonNull이 붙은 필드를 모두 파라미터로 받는 생성자 자동 생성
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    private final VideoService videoService;


    /**
     * 지정된 쿼리 매개변수와 일치하는 검색 결과 컬렉션을 반환*
     *
     * @param part 운동 부위(특정 or 전체), duration 영상 길이(특정 or 전체), recommend 추천 방식(필수)
     * @return 영상 제목 + 링크 목록
     */
    @GetMapping("/search")
    public YoutubeVideo searchYoutubeVideos(@RequestParam String part,
                                            @RequestParam(required = false, defaultValue = "") String duration,
                                            @RequestParam(required = false, defaultValue = "") String recommend,
                                            HttpSession session) {

        String searchQuery = "운동";

        // part가 "all"이면 운동만 서치
        if ("전체".equalsIgnoreCase(part)) {
            part = searchQuery;
        } else { //정해진 부위가 있다면 부위 + "운동"
            part = part + searchQuery;
        }

        // duration도 "all"이면 빈 문자열 처리
        if ("전체".equalsIgnoreCase(duration)) {
            duration = "";
        }

        //랜덤 영상 리스트(만약 사용자가 내가 뽑아낸 영상을 다 넘겼다면? 어떻게 해야할지 생각이 필요하다)
        //재추천 로직이 필요한것으로 생각됩니다(기존 리스트랑 겹치지 않게)
        List<YoutubeVideo> videos = videoService.searchVideos(part, duration, recommend);

        if (videos.isEmpty()) { //만약 영상이 없다면 오류 상태 던지기
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "영상 없음");
        }

        session.setAttribute("videoList", videos); //세션에 영상 리스트 저장하기

        //여러개의 영상을 랜덤으로 하나 픽하기
        int randomIdx = new Random().nextInt(videos.size());
        YoutubeVideo video = videos.get(randomIdx);


        //뽑은 영상 리스트 중에 하나 뽑아서 프론트로 던지기
        return video;

    }

    /**
     * @videoId 스킵하고자 하는 영상의 아이디
     **/
    @GetMapping("/reSearch")
    public YoutubeVideo reSearchYoutubeVideos(@RequestParam long youtubeVideoId, HttpSession session) {

        List<YoutubeVideo> videoList = (List<YoutubeVideo>) session.getAttribute("videoList");

        if (videoList == null || videoList.isEmpty()) { //저장된 영상 리스트가 없을 때
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 영상 리스트가 없습니다");
        }

        // videoId에 해당하는 영상 제거
        videoList.removeIf(video -> video.getYoutubeVideoId() == youtubeVideoId);

        if (videoList.isEmpty()) {
            //추천할 영상이 더이상 없을 때
            //프론트에서 다시 /search 요청 보내도록 해야함 🖥️
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 영상이 더 이상 없습니다");
        }

        // 남은 리스트 중 랜덤 추천
        int randomIdx = new Random().nextInt(videoList.size());
        YoutubeVideo nextVideo = videoList.get(randomIdx);

        // 변경된 리스트 다시 세션에 저장
        session.setAttribute("videoList", videoList);

        for (YoutubeVideo video : videoList) {
            System.out.println(video.getYoutubeVideoId());
        }

        //다음 영상 추천하기
        return nextVideo;

    }
    
    /**
     * Videos 화면에서 받아온 비디오 값 저장하여 전달
     * **/
    //루틴에 영상들 저장하기
    @PostMapping("/insertVideoRoutine")
    public String insertVideo(@RequestBody List<YoutubeVideo> youtubeVideoList, HttpSession session) {

        //프론트에서 영상 여러 개를 선택해 리스트에 담아두고 루틴 생성을 위해 저장 🖥️

        //업로드 영상은 세션에서 꺼내오기
        List<UploadedVideo> uploadedVideoList = (List<UploadedVideo>) session.getAttribute("uploadVideoList");

        // 0으로 초기화(랜덤으로 영상을 뽑기 위해 넣은 임의 id 이므로 초기화)
        for(YoutubeVideo youtubeVideo : youtubeVideoList){
            youtubeVideo.setYoutubeVideoId(0L);
        }

        //해당 비디오 루틴에 저장하러 가기(유튜브 영상, 업로드 영상)
        int result = videoService.insertVideoRoutine(youtubeVideoList, uploadedVideoList);

        //영상 루틴 출력해줘야함
        return "";
    }

    /***
     * @file 내가 업로드한 영상 파일
     * @UploadedVideo 그 외 영상 정보
     * */
    //내가 찍은 영상 올리기
    @PostMapping("/myUpload")
    public ResponseEntity<UploadedVideo> uploadVideo(@RequestParam("file") MultipartFile file,
                                                     @RequestParam String title,
                                                     @RequestParam String part,
                                                     @RequestParam int durationSeconds,
                                                     HttpSession session) {

        UploadedVideo uploadedVideo = new UploadedVideo();
        uploadedVideo.setTitle(title);
        uploadedVideo.setPart(part);
        uploadedVideo.setDurationSeconds(durationSeconds);

        //영상 S3에 저장하러가기
        UploadedVideo videoDTO = videoService.uploadVideo(file, uploadedVideo);

        // 세션에서 리스트 꺼내기
        List<UploadedVideo> uploadVideoList = (List<UploadedVideo>) session.getAttribute("uploadVideoList");

        // 비어있으면 새 리스트 생성
        if (uploadVideoList == null) {
            uploadVideoList = new ArrayList<>();
        }


        //만약에 전체 루틴 생성 그만두게 되면 세션에서 이 값은 삭제해야 할듯
        //그리고 이 영상을 삭제하는 버튼을 누를때도 삭제해야함
        // 새 업로드된 영상 추가
        uploadVideoList.add(videoDTO);

        // 세션에 다시 저장
        session.setAttribute("uploadVideoList", uploadVideoList);

        return ResponseEntity.ok(videoDTO);
    }

}