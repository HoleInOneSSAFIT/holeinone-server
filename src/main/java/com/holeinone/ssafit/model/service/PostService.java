package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;

import java.util.List;

public interface PostService {

    //루틴 아이디를 통해 루틴 영상 정보 조회
    List<VideoRoutineSessionData> getRoutineById(Long routineId);

}
