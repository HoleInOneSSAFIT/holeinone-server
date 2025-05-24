package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentResponse;
import com.holeinone.ssafit.model.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    // 게시글에 대한 리뷰 조회
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentList(@PathVariable Long postId) {
        List<CommentResponse> commentList = commentService.getCommentList(postId);
        return ResponseEntity.ok((commentList));
    }
}
