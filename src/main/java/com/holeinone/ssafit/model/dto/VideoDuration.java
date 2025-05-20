package com.holeinone.ssafit.model.dto;

import lombok.Data;

//영상 길이 매핑
@Data
public class VideoDuration {
    private Long id; //카테고리 ID
    private Long videoId; //영상 ID(FK VIDEO 테이블의 ID)
    private Integer categoryId; //카테고리 ID(FK CategoryDuration 테이블 ID)

}
