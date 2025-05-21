package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//게시글 첨부 파일 테이블
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostFile {

    private Long postFileId; //파일 ID
    private String fileUrl; //파일 URL
    private String originalFilename; //기존 파일 이름
    private String fileType; // 파일 타입 'IMAGE' or 'VIDEO'
    private LocalDateTime uploadedAt; //업로드 시간
    private Long postId; //게시글 ID
    
}
