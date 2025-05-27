package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.CommentDao;
import com.holeinone.ssafit.model.dto.Comment;
import com.holeinone.ssafit.model.dto.CommentRequest;
import com.holeinone.ssafit.model.dto.CommentResponse;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
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

    @Override
    public Comment findById(Long commentId) {
        return commentDao.findById(commentId);
    }

    @Override
    public void updateComment(Comment comment){
        commentDao.updateComment(comment);
    }

    @Override
    public String deleteComment(Long commentId, Long userId) throws NotFoundException, AccessDeniedException {
        Comment comment = commentDao.findById(commentId);

        if (comment == null || comment.getIsDeleted()) { // is_deleted(default:0)
            throw new NotFoundException("존재하지 않거나 삭제된 댓글 입니다.");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 삭제할 수 있습니다.");
        }

        // 대댓글 존재여부 확인(commentId는 부모ID - 자식 댓글 가지고 있는지 파악) - true(가지고있다) false(없다)
        Boolean hasChild = commentDao.hasChildComment(commentId);

        // soft delete
        commentDao.softDeleteComment(commentId);

        return hasChild ? "(soft-delete) '(삭제된 댓글 입니다.)' 삭제" : "(soft-delete) UI 상에 안보이게 삭제";
    }

}
