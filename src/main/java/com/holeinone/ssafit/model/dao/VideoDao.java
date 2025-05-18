package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.Routine;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.Videos;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface VideoDao {

    //루틴에 영상 저장하기
    int insertVideoRoutine(Videos videos);

    //운동 루틴 아이디 생성
    long createRoutine(Routine routine);
    
    //운동-루틴 매핑
    int insertRoutineVideo(RoutineVideo rv);
}
