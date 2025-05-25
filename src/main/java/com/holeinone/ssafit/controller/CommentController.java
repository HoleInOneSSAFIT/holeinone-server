package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.model.service.CommentService;
import com.holeinone.ssafit.model.service.UserService;
import com.holeinone.ssafit.security.JwtUtil;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final UserService userService;

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
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@RequestHeader("Authorization") String token,
                                                @PathVariable Long commentId,
                                                @RequestBody CommentRequest request) {
        // 파싱 먼저 하고
        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userService.getInfo(token);
        Long userId = user.getUserId();

        Comment comment = commentService.findById(commentId);

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 댓글입니다.");
        }

        if (!comment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(request.getContent());

        commentService.updateComment(comment);

        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@RequestHeader("Authorization") String token,
                                                @PathVariable Long commentId)
            throws AccessDeniedException, NotFoundException {
        // 파싱 먼저 하고
        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userService.getInfo(token);
        Long userId = user.getUserId();

        String deleteResult = commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok((deleteResult));
    }
}
