package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import com.holeinone.ssafit.model.service.CommentService;
import com.holeinone.ssafit.security.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    // 게시글에 대한 댓글 조회
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentList(@PathVariable Long postId) {
        List<CommentResponse> commentList = commentService.getCommentList(postId);
        return ResponseEntity.ok((commentList));
    }

    // 댓글 작성
    @PostMapping("/write")
    public ResponseEntity<String> writeComment(@RequestHeader("Authorization") String token,
                                               @PathVariable Long postId,
                                               @RequestBody CommentRequest request) {
        // 파싱 먼저 하고
        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 보낼 값 추출하기
        Long userId = jwtUtil.extractUserId(token);
        request.setPostId(postId);

        commentService.writeComment(userId, request);

        return ResponseEntity.ok("댓글 등록이 완료되었습니다."); // response는 db로 확인하기 -  void
    }

    // 댓글 수정

    // 댓글 삭제
}
