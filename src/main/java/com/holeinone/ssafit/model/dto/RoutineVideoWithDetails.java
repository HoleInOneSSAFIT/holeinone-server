package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineVideoWithDetails {

    private Long routineVideoId; //운동-루틴 매핑 아이디
    private int sequenceOrder; // 운동 순서
    private Long routineId; // 루틴 아이디

    private YoutubeVideo youtubeVideo; // 유튜브 영상 정보
    private UploadedVideo uploadedVideo; // 업로드 영상 정보

}
