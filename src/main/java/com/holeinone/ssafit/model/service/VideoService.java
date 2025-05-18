package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.Videos;

import java.util.List;

public interface VideoService {
    
    //유튜브 영상 검색
    List<Videos> searchVideos(String part, String duration, String recommend);
    
    //루틴에 우동 영상 저장하기
    int insertVideoRoutine(List<Videos> video);
}
