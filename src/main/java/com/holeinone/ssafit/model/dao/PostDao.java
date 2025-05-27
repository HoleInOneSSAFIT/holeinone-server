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

    //게시글 최신순 조회
    List<Post> getLatestPosts();

    //게시글 인기순 조회
    List<Post> getPopularPosts();

    //게시글 운동 부위별 조회
    List<Post> getPostsByPart(String part);

    // 게시글 정보 수정 (루틴은 수정하지 않음)
    int updatePost(Post post);

    // 게시글 관련 파일 삭제
    //todo: 안쓰면 지우기
    int deleteFiles(Long postId);

    // 게시글에 새로운 파일 추가
    int insertFile(PostFile postFile);

    //게시글 파일 삭제
    void deleteFileById(Long postFileId);
    
    //게시글 좋아요 눌렀는지 확인
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    //좋아요 삭제
    void deleteLike(Long postId, Long userId);
    
    //좋아요 삽입
    void insertLike(PostLike postLike);
    
    //좋아요 개수 카운트
    int countLikes(Long postId);

    // 게시글 상세페이지 조회 시 조회수 1 증가
    void increaseViewCount(Long postId);
    
    //조회수 리턴
    int viewCount(Long postId);
    
    //게시글 댓글 수 반환
    int getCommentCount(Long postId);
}
