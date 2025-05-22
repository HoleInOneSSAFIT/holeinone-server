package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.*;

import java.util.List;

public interface PostService {

    //루틴 아이디를 통해 루틴 영상 정보 조회
    List<VideoRoutineSessionData> getRoutineById(Long routineId);

    // 게시글 정보 전달 후 게시글 아이디 반환
    Long postRoutine(PostDetailInfo postDetailInfo);

    // 게시글 상세 정보 가져오기
    Post getPost(Long postId);

    // 게시글 파일 정보 가져오기
    List<PostFile> getFiles(Long postId);

    // 루틴 정보 가져오기
    Routine getRoutine(Long routineId);

    //게시글 작성한 유저 정보 반환
    User getPostUser(Long postId);
    
    //게시글 삭제
    int deletePost(Long postId);
    
    //게시글 전체 목록
    List<Post> listPosts();
}
