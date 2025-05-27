package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentResponse;
import java.util.List;

public interface CommentDao {
    List<CommentResponse> selectAllComments(Long postId);
    List<CommentResponse> selectAllMyComments(Long userId);
    void insertComment(Comment comment);
    Comment findById(Long CommentId);
    void updateComment(Comment comment);
    Boolean hasChildComment(Long commentId); // 댓글 삭제할때 대댓글 있는지 확인
    void softDeleteComment(Long commentId);
}
