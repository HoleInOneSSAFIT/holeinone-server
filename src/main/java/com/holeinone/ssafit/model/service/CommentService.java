package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.CommentResponse;
import java.util.List;

public interface CommentService {
    List<CommentResponse> getCommentList(Long postId);
}
