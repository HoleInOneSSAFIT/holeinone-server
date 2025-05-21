package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.Post;
import com.holeinone.ssafit.model.dto.PostDetailInfo;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;

import java.util.List;

public interface PostService {

    //루틴 아이디를 통해 루틴 영상 정보 조회
    List<VideoRoutineSessionData> getRoutineById(Long routineId);

    // 게시글 정보 전달 후 게시글에 해당하는 정보 얻어오기
    List<PostDetailInfo> postRoutine(PostDetailInfo postDetailInfo);
}
