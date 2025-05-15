package com.holeinone.ssafit.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

//게시글 첨부 파일 테이블
@Data
public class PostFile {

    private Long postFileId; //파일 ID
    private String fileUrl; //파일 URL
    private String fileType; // 파일 타입 'IMAGE' or 'VIDEO'
    private LocalDateTime uploadedAt; //업로드 시간
    private Long postId; //게시글 ID
    
}
