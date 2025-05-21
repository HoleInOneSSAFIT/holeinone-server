package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Post;
import com.holeinone.ssafit.model.dto.PostDetailInfo;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;
import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

    //게시글 등록( 텍스트 필드 + 파일을 한꺼번에 받을 때 명시)
    @PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postRoutine(@ModelAttribute PostDetailInfo postDetailInfo) {
        
        // 게시글 정보 전달 후 게시글에 해당하는 정보 얻어오기
        List<PostDetailInfo> postDetailInfoResult = postService.postRoutine(postDetailInfo);


        return ResponseEntity.ok("");
    }

    //게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetailById(@PathVariable Long postId) {

        //루틴 아이디를 통해 루틴 영상 정보 조회
        List<VideoRoutineSessionData> routineVideoList = postService.getRoutineById(postId);

        return ResponseEntity.ok("");
    }


}
