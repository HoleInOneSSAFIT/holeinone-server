package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.*;
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
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final VideoService videoService;

    //게시글 등록( 텍스트 필드 + 파일을 한꺼번에 받을 때 명시)
    @PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postRoutine(@ModelAttribute PostDetailInfo postDetailInfo) {
        
        // 게시글 정보 전달 후 게시글 아이디 반환
        Long postId  = postService.postRoutine(postDetailInfo);

        return ResponseEntity.ok(Map.of("postId", postId));
    }

    // 게시글 상세 정보 가져오기
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        log.info("get post by id: {}", postId);
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // 게시글 파일 정보 가져오기
    @GetMapping("/{postId}/files")
    public ResponseEntity<List<PostFile>> getPostFiles(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getFiles(postId));
    }

    // 루틴 정보 가져오기
    @GetMapping("/routine/{routineId}/info")
    public ResponseEntity<Routine> getRoutine(@PathVariable Long routineId) {
        return ResponseEntity.ok(postService.getRoutine(routineId));
    }

    //특정 루틴 정보 조회(게시글 작성 시 함께 보여줄 용도)
    @GetMapping("/routine/{routineId}")
    public ResponseEntity<?> getRoutineById(@PathVariable Long routineId) {

        //루틴 아이디를 통해 루틴 영상 정보 조회
        List<VideoRoutineSessionData> routineVideoList = postService.getRoutineById(routineId);

        log.info("특정 루틴 정보 조회 : {}", routineVideoList);

        return ResponseEntity.ok(routineVideoList);
    }

    // 댓글 정보 가져오기(댓글 기능 완성 시 가져오기)
//    @GetMapping("/post/{postId}/comments")
//    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
//        return ResponseEntity.ok(postService.getCommentsByPost(postId));
//    }

    //게시글 작성한 유저 정보
    @GetMapping("/user/{postId}")
    public ResponseEntity<User> getPostUser(@PathVariable Long postId) {
        
        //게시글 작성한 유저 정보 반환
        User postUserList = postService.getPostUser(postId);

        //유저의 프로필 이미지 정보도 가져오기!!!!!!!!!!!!!!!!

        return ResponseEntity.ok(postUserList);
    }
    
    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        
        //게시글 삭제
        int result = postService.deletePost(postId);
       
        //반환값 생각해봐야함
        return ResponseEntity.ok("게시글 삭제 성공");
    }

    //게시글 전체 목록
    @GetMapping("/all")
    public ResponseEntity<?> listPosts() {

        List<Post> postList = postService.listPosts();

        log.info("전체 게시글 조회하기 : {}", postList);

        return ResponseEntity.ok(postList);
    }

    //게시글 좋아요
    @PostMapping("/like/{postId}")
    public ResponseEntity<?> PostLike(@PathVariable Long postId) {
        
        //좋아요 버튼 클릭
        int result = postService.postLike(postId);

        return ResponseEntity.ok("");

    }

    //게시글 최신순 조회
    @GetMapping("/latest")
    public ResponseEntity<List<Post>> getLatestPosts() {
        List<Post> posts = postService.getLatestPosts();
        return ResponseEntity.ok(posts);
    }

    //게시글 인기순 조회
    /**view_count: 1점
     like_count: 3점
     comment_count: 5점**/
    @GetMapping("/popular")
    public ResponseEntity<List<Post>> getPopularPosts() {
        return ResponseEntity.ok(postService.getPopularPosts());
    }



}
