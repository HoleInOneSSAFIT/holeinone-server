package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {

    private int likeCount;     // 현재 게시글의 좋아요 수
    private boolean likedByMe; // 사용자가 좋아요를 눌렀는지 여부

}
