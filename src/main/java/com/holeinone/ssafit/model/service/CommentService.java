package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import java.util.List;

public interface CommentService {
    List<CommentResponse> getCommentList(Long postId);
    List<CommentResponse> getMyCommentList(Long userId);
    void writeComment(Long userId, CommentRequest request);
    Comment findById(Long commentId);
    void updateComment(Comment comment);
}
