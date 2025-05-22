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

    // 게시글 상세 정보 가져오기
    Post getPost(Long postId);

    // 게시글 파일 정보 가져오기
    List<PostFile> getFiles(Long postId);

    // 루틴 정보 가져오기
    Routine getRoutine(Long routineId);

    //루틴 공유 상태 ture로 변경
    void postRoutineShared(Long routineId);

    //게시글 작성한 유저 정보 반환
    User getPostUser(Long postId);

    //삭제할 게시글 파일 url 가져오기
    List<String> selectFileUrlsByPostId(Long postId);

    //db에서 게시글 삭제하고 파일 url 얻어오기
    int deletePost(Long postId);
    
    //게시글 전체 목록 조회
    List<Post> listPosts();

    // userId로 본인이 작성한 게시글 목록 가져오기
    List<Post> selectPostList(Long userId);

}
