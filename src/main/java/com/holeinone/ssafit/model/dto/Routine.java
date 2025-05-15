package com.holeinone.ssafit.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

//루틴
@Data
public class Routine {

    private Long routineId; //루틴 ID
    private Boolean isShared; //커뮤니티 공유 여부
    private LocalDateTime createdAt; //생성일
    private LocalDateTime updatedAt; //수정일
    private Long userId; //유저 ID(FK USER 테이블 ID)

}
