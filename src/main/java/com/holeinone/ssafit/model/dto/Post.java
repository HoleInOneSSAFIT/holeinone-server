package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long postId; //게시글 ID
    private String title; //게시글 제목
    private String content; //게시글 내용
    private String thumbnailUrl; //대표 썸네일
    private Integer viewCount; //조회수
    private Integer commentCount; //댓글 수
    private LocalDateTime createdAt; //생성일
    private LocalDateTime updatedAt; //수정일
    private Long userId; //유저 ID (FK USER 테이블 ID)
    private Long routineId; //루틴 ID (FK Routine 테이블 ID)

}
