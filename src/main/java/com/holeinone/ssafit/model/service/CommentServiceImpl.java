package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.CommentDao;
import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;

    @Override
    public List<CommentResponse> getCommentList(Long postId) {
        return commentDao.selectAllComments(postId);
    }

    @Override
    public void writeComment(Long userId, CommentRequest request) {
        Comment comment = new Comment();

        comment.setUserId(userId);
        comment.setPostId(request.getPostId());
        comment.setContent(request.getContent());

        commentDao.insertComment(comment);
    }

    @Override
    public List<CommentResponse> getMyCommentList(Long userId) {
        return commentDao.selectAllMyComments(userId);
    }

}
