package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.Videos;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface VideoDao {

    //루틴에 영상 저장하기
    int insertVideoRoutine(Videos video);
}
