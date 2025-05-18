package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//영상
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Videos {

    private Long videoId; //영상 ID
    private String sourceType; // 영상 출처 'YOUTUBE' or 'UPLOAD'
    private String videoUrl; //영상 URL
    private String title; //영상 제목
    private Integer durationSeconds; //영상 길이(초단위)
    private Integer restSecondsAfter; //운동 후 쉬는 시간
    private String part; //운동 부위
    private String channelName; //유튜브 채널명
    private LocalDateTime createdAt; //등록일
    private LocalDateTime updatedAt; //수정일
    private Long userId; //유저 고유 ID (FK user 테이블 ID)
}
