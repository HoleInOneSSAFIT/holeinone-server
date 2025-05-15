package com.holeinone.ssafit.model.dto;

import lombok.Data;

//운동 루틴
@Data
public class RoutineVideo {

    private Long routineVideoId; //매핑 ID
    private Integer sequenceOrder; //루틴 내 순서
    private Integer restSecondsAfter; //영상 이후 쉬는 시간
    private Long routineId; //루틴 ID(FK ROUTINE 테이블 ID)
    private Long videoId; //비디오 ID(FK VIDEO 테이블 ID)
}
