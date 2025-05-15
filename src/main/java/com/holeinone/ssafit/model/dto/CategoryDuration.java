package com.holeinone.ssafit.model.dto;

import lombok.Data;

//영상 길이 카테고리 테이블
@Data
public class CategoryDuration {

    private Integer categoryId; //카테고리 ID
    private String name; // 카테고리 이름 예: 짧은 운동, 중간 길이 운동, 긴 운동

}
