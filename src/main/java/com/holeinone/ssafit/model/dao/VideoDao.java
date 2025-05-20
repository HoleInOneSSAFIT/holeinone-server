package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.Routine;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.YoutubeVideo;

public interface VideoDao {

    //루틴에 유튜브 영상 저장하기
    int insertVideoRoutine(YoutubeVideo youtubeVideo);

    //영상 직접 업로드(db 저장)
    int insertUploadedRoutine(UploadedVideo uploadedVideo);

    //운동 루틴 아이디 생성
    long createRoutine(Routine routine);
    
    //운동-루틴 매핑(유튜브)
    int insertRoutineYoutubeVideo(RoutineVideo rv);

    //운동-루틴 매핑(유튜브)
    int insertRoutineUploadedVideo(RoutineVideo rv);
}
