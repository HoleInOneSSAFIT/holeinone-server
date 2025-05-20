package com.holeinone.ssafit.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

//사용자 영상 추천 히스토리
@Data
public class VideoRecommendHistory {

    private Long historyId; //영상 추천 히스토리 ID
    private Boolean isSkipped; //사용자 영상 넘김 여부
    private LocalDateTime createdAt; //추천 시점
    private Long userId; //ID (FK USER 테이블 ID)
    private Long videoId; //ID (FK VIDEO 테이블의 ID)

}
