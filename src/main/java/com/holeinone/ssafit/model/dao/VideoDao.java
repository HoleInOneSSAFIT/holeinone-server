package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.*;

import java.util.List;

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

    //저장한 유튜브 객체 반환(아이디를 통해 조회)
    YoutubeVideo selectYoutubeVideoById(int youtubeVideoResultId);

    //루틴 삭제를 위한 s3 유튜브, 업로드 영상 url 조회
    List<String> routineFileUrl(long routineId);

    //루틴 삭제를 위한 s3 루틴 파일 url 조회
    List<String> routinePostFileUrl(long routineId);

    //게시글 url 파일 가져오기(루틴 아이디를 공유한 게시글 아이디를 통해서)
    List<String> postFileUrl(long routineId);

    //루틴 삭제
    int routineIdDelete(long routineId);

    //루틴-영상 매핑 테이블에서 유튜브 영상ID, 업로드 영상 ID를 가져와서 유튜브 영상 테이블, 업로드 영상 테이블에서 삭제
    List<RoutineVideoId> routineVideoIds(long routineId);

    //루틴 삭제 -> 유튜브 영상 db 삭제
    void deleteYoutubeById(Long youtubeVideoId);

    //루틴 삭제- > 업로드 영상 db 삭제
    void deleteUploadedById(Long uploadedVideoId);
}
