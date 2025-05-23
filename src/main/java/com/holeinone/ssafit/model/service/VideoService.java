package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.exception.CustomException;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.YoutubeVideo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    
    //유튜브 영상 검색
    List<YoutubeVideo> searchVideos(String part, String duration, String recommend);
    
    //루틴에 운동 영상 저장하기
    int insertVideoRoutine(List<YoutubeVideo> youtubeVideoList,
                           List<UploadedVideo> uploadedVideoList,
                           String routineTitle, String routineContent);

    //영상 직접 업로드
    UploadedVideo uploadVideo(MultipartFile file, UploadedVideo uploadedVideo);

    //내가 올린 유튜브 url
    YoutubeVideo directYoutubeUrl(String url, String part, int sequence ,int restSecondsAfter) throws CustomException;

    //루틴 아이디를 통해 루틴 삭제
    int routineIdDelete(long routineId);
}
