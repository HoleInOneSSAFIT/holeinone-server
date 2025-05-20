package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//직접 업로드한 영상
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedVideo {

    private Long uploadedVideoId; // 영상 ID
    private String videoUrl; // S3에 업로드된 URL
    private String originalFilename; // 업로드된 파일의 원래 이름
    private String title; // 영상 제목 (선택 입력 가능)
    private String part; // 운동 부위
    private Integer durationSeconds; // 영상 길이 (초 단위)
    private LocalDateTime createdAt; // 등록일 (기본값: 현재 시간)
    private LocalDateTime updatedAt; // 수정일 (업데이트 시 자동 변경)
    private Integer uploadedSequence; //영상 순서
    private Long userId; // 업로드한 사용자 ID (FK)

}
