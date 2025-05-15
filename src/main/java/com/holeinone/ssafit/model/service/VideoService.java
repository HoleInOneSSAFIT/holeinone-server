package com.holeinone.ssafit.model.service;

import java.util.List;

public interface VideoService {
    
    //유튜브 영상 검색
    List<String> searchVideos(String searchQuery, String duration, String recommend);
}
