package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Post;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;
import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final VideoService videoService;
    
    //특정 루틴 정보 조회(게시글 작성 시 함께 보여줄 용도)
    @GetMapping("/routine/{routineId}")
    public ResponseEntity<?> getRoutineById(@PathVariable Long routineId) {

        //루틴 아이디를 통해 루틴 영상 정보 조회
        List<VideoRoutineSessionData> routineVideoList = postService.getRoutineById(routineId);
        
        return ResponseEntity.ok(routineVideoList);
    }


}
