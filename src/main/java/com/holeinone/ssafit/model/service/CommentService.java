package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import java.nio.file.AccessDeniedException;
import java.util.List;
import org.apache.ibatis.javassist.NotFoundException;

public interface CommentService {
    List<CommentResponse> getCommentList(Long postId);
    List<CommentResponse> getMyCommentList(Long userId);
    void writeComment(Long userId, CommentRequest request);
    Comment findById(Long commentId);
    void updateComment(Comment comment);
    String deleteComment(Long commentId, Long userId) throws NotFoundException, AccessDeniedException;
}
