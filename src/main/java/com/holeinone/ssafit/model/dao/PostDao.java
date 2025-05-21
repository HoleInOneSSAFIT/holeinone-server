package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.*;

import java.util.List;

public interface PostDao {

    //루틴 아이디를 통해 루틴 영상 정보 조회
    List<RoutineVideo> getRoutineById(Long routineId);
    
    //루틴 아이디를 통한 유튜브 영상 정보 조회
    YoutubeVideo getYoutubeById(Long youtubeVideoId);

    //루틴 아이디를 통한 업로드 영상 정보 조회
    UploadedVideo getUploadById(Long uploadedVideoId);

    //게시글 등록받고 아이디 반환
    int postRoutine(Post postInfo);

    //post 테이블 썸네일 url 등록
    int postRoutinethumbnailUrl(Post postInfo);
    
    //게시글 파일 저장
    int postDaoFileInsert(PostFile postFile);
}
