package com.holeinone.ssafit.model.dto;

import lombok.Data;

//영상 운동 부위 매핑
@Data
public class VideoBodyPart {
    private Long id; //매핑 ID
    private Long videoId; //영상 ID(FK video 테이블 ID)
    private Integer categoryId; //카테고리 ID(FK categoryBodyPart ID)

}
