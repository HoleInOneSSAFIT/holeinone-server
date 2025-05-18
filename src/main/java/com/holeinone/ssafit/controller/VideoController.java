package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Videos;
import com.holeinone.ssafit.model.service.VideoService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public Videos searchYoutubeVideos(@RequestParam String part,
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
        List<Videos> videos = videoService.searchVideos(part, duration, recommend);

        if (videos.isEmpty()) { //만약 영상이 없다면 오류 상태 던지기
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "영상 없음");
        }

        session.setAttribute("videoList", videos); //세션에 영상 리스트 저장하기

        //여러개의 영상을 랜덤으로 하나 픽하기
        int randomIdx = new Random().nextInt(videos.size());
        Videos video = videos.get(randomIdx);


        //뽑은 영상 리스트 중에 하나 뽑아서 프론트로 던지기
        return video;

    }

    /**
     * @videoId 스킵하고자 하는 영상의 아이디
     **/
    @GetMapping("/reSearch")
    public Videos reSearchYoutubeVideos(@RequestParam long videoId, HttpSession session) {

        List<Videos> videoList = (List<Videos>) session.getAttribute("videoList");

        if (videoList == null || videoList.isEmpty()) { //저장된 영상 리스트가 없을 때
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 영상 리스트가 없습니다");
        }

        // videoId에 해당하는 영상 제거
        videoList.removeIf(video -> video.getVideoId() == videoId);

        if (videoList.isEmpty()) {
            //추천할 영상이 더이상 없을 때
            //프론트에서 다시 /search 요청 보내도록 해야함 🖥️
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 영상이 더 이상 없습니다");
        }

        // 남은 리스트 중 랜덤 추천
        int randomIdx = new Random().nextInt(videoList.size());
        Videos nextVideo = videoList.get(randomIdx);

        // 변경된 리스트 다시 세션에 저장
        session.setAttribute("videoList", videoList);

        for (Videos video : videoList) {
            System.out.println(video.getVideoId());
        }

        //다음 영상 추천하기
        return nextVideo;

    }
    
    /**
     * Videos 화면에서 받아온 비디오 값 저장하여 전달
     * **/
    //루틴에 해당 영상 저장하기
    @PostMapping("/insertVideoRoutine")
    public String insertVideo(@RequestBody Videos video ) {

        video.setVideoId(0L); // 0으로 초기화(랜덤으로 영상을 뽑기 위해 넣은 임의 id 이므로 초기하 필요)

        //해당 비디오 루틴에 저장하러 가기
        int result = videoService.insertVideoRoutine(video);


        return "";
    }
}