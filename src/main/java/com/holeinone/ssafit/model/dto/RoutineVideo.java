package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//루틴 영상 매핑
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineVideo {

    private Long routineVideoId;       // 기본키
    private Integer sequenceOrder;     // 루틴 내 순서
    private Long routineId;            // 루틴 ID (FK)
    private Long youtubeVideoId;              // 유튜브 영상 ID (FK)
    private Long uploadedVideoId;          // 직접 업로드한 영상 ID (FK)
}
