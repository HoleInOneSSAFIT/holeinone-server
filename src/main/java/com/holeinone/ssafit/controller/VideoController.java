package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

//final 또는 @NonNull이 붙은 필드를 모두 파라미터로 받는 생성자 자동 생성
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    private final VideoService videoService;

    /**
     * 지정된 쿼리 매개변수와 일치하는 검색 결과 컬렉션을 반환*
     * @param part 운동 부위(특정 or 전체), duration 영상 길이(특정 or 전체), recommend 추천 방식(필수)
     * @return  영상 제목 + 링크 목록*/
    @GetMapping("/search")
    public List<String> searchYoutubeVideos(@RequestParam String part,
                                            @RequestParam(required = false, defaultValue = "") String duration,
                                            @RequestParam(required = false, defaultValue = "") String recommend) {

        String searchQuery = "운동";

        // part가 "all"이면 운동만 서치
        if ("all".equalsIgnoreCase(part)) {
            part = searchQuery;
        } else { //정해진 부위가 있다면 부위 + "운동"
            part = part + searchQuery;
        }

        // duration도 "all"이면 빈 문자열 처리
        if ("all".equalsIgnoreCase(duration)) {
            duration = "";
        }


        return videoService.searchVideos(part, duration, recommend);


    }
}
