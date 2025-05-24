package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.CommentResponse;
import java.util.List;

public interface CommentDao {
    List<CommentResponse> selectAllComments(Long postId);
}
