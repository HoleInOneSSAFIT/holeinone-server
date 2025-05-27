package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//게시글 좋아요
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    private Long postLikeId;  // 좋아요 고유 ID
    private Long postId;      // 게시글 ID
    private Long userId;      // 사용자 ID
    private LocalDateTime createdAt;  // 좋아요 생성 시각

}
