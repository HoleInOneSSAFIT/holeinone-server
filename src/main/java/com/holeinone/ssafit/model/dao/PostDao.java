package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.YoutubeVideo;

import java.util.List;

public interface PostDao {

    //루틴 아이디를 통해 루틴 영상 정보 조회
    List<RoutineVideo> getRoutineById(Long routineId);
    
    //루틴 아이디를 통한 유튜브 영상 정보 조회
    YoutubeVideo getYoutubeById(Long youtubeVideoId);

    //루틴 아이디를 통한 업로드 영상 정보 조회
    UploadedVideo getUploadById(Long uploadedVideoId);
}
