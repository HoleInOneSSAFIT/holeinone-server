package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.*;
import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.VideoService;
import com.holeinone.ssafit.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final AuthUtil authUtil;

    //게시글 등록( 텍스트 필드 + 파일을 한꺼번에 받을 때 명시)
    @PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postRoutine(@ModelAttribute PostDetailInfo postDetailInfo, @RequestHeader("Authorization") String token) {
        
        // 게시글 정보 전달 후 게시글 아이디 반환
        Long postId  = postService.postRoutine(postDetailInfo, token);

        return ResponseEntity.ok(Map.of("postId", postId));
    }

    // 게시글 상세 정보 가져오기
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {

        Post post = postService.getPost(postId);

        log.info("게시글 상세 젇보 : {}", post);

        return ResponseEntity.ok(post);
    }

    // 게시글 파일 정보 가져오기
    @GetMapping("/{postId}/files")
    public ResponseEntity<List<PostFile>> getPostFiles(@PathVariable Long postId) {

        List<PostFile> getPostFile = postService.getFiles(postId);

        log.info("게시글 파일 정보 가져오기 : {}", getPostFile);

        return ResponseEntity.ok(getPostFile);
    }

    // 루틴 정보 가져오기
    @GetMapping("/routine/{routineId}/info")
    public ResponseEntity<Routine> getRoutine(@PathVariable Long routineId) {

        Routine routineInfo = postService.getRoutine(routineId);
        
        log.info("루틴 정보(제목 내용) : {}", routineInfo);
        
        return ResponseEntity.ok(routineInfo);
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
        log.info("게시글 작성한 유저 정보 : {}", postUserList);

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

        List<Post> posts = postService.getPopularPosts();

        log.info("인기 순 게시글 : {}", posts);

        return ResponseEntity.ok(posts);
    }

    //루틴 영상 중 하나라도 찾는 부위가 있으면 게시글 반환
    @GetMapping("/part")
    public ResponseEntity<List<Post>> getPostsByPart(@RequestParam String part) {

        log.info("커뮤니티 게시글 부위별 검색: {} ", part);

        List<Post> posts = postService.getPostsByPart(part);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PutMapping(value = "/update/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @ModelAttribute PostDetailInfo postDetailInfo,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "filesToDelete", required = false) List<Long> filesToDelete,
            @RequestHeader("Authorization") String token) {

        try {
            log.info("첨부 파일 수: {}", files != null ? files.size() : 0);
            log.info("삭제할 파일 수: {}", filesToDelete != null ? filesToDelete.size() : 0);

            postDetailInfo.setFiles(files);
            postDetailInfo.setFilesToDelete(filesToDelete);

            postService.updatePost(postId, postDetailInfo, token);

            return ResponseEntity.ok("게시글 수정 완료");
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 수정 실패: " + e.getMessage());
        }
    }

    //현재 접속한 유저 정보 제공
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {

            //유저 아이디 반환
            Long userId = authUtil.extractUserIdFromToken(token);

            return ResponseEntity.ok(userId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 게시글 좋아요를 누르거나 취소
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String token) {
        LikeResponse response = postService.toggleLike(postId, token);
        return ResponseEntity.ok(response);
    }

    // 게시글의 좋아요 수 및 내가 좋아요 눌렀는지 여부 조회
    @GetMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> getLikeInfo(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String token) {
        LikeResponse response = postService.getLikeInfo(postId, token);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세페이지 조회 시 조회수 1 증가
    @PostMapping("/{postId}/increase-view")
    public ResponseEntity<?> increaseViewCount(@PathVariable Long postId) {
        int viewCount = postService.increaseViewCount(postId);
        return ResponseEntity.ok(viewCount); //증가된 조회수 리턴
    }
    
    //상세 게시글 댓글 수 반환하기
    @GetMapping("/{postId}/commentCount")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {
        
        //게시글 댓글 수 반환
        int commentCount = postService.getCommentCount(postId);

        log.info("게시글 댓글 수 반환  :{}", commentCount);

        return ResponseEntity.ok(commentCount);
    }


}
