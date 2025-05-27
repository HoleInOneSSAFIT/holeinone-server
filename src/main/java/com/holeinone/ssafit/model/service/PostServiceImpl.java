package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.PostDao;
import com.holeinone.ssafit.model.dao.UserDao;
import com.holeinone.ssafit.model.dao.VideoDao;
import com.holeinone.ssafit.model.dto.*;
import com.holeinone.ssafit.util.AuthUtil;
import com.holeinone.ssafit.util.S3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PostServiceImpl implements PostService{

    private final PostDao postDao;
    private final S3Uploader s3Uploader;
    private final UserDao userDao;
    private final AuthUtil authUtil;

    public PostServiceImpl(PostDao postDao, S3Uploader s3Uploader, UserDao userDao, AuthUtil authUtil) {
        this.postDao = postDao;
        this.s3Uploader = s3Uploader;
        this.userDao = userDao;
        this.authUtil = authUtil;
    }

    //루틴 아이디를 통해 루틴 영상 정보 조회
    @Override
    public List<VideoRoutineSessionData> getRoutineById(Long routineId) {

        // 1. 루틴 아이디를 통해 루틴 영상 정보 조회
        List<RoutineVideo> routineVideoList = postDao.getRoutineById(routineId);

        // 루틴이 존재하지 않을 경우 예외 발생
        if (routineVideoList == null || routineVideoList.isEmpty()) {
            throw new IllegalArgumentException("해당 루틴 ID(" + routineId + ")에 대한 영상 정보가 존재하지 않습니다.");
        }

        // 루틴 영상을 담을 리스트
        VideoRoutineSessionData videoRoutineSessionData = new VideoRoutineSessionData();

        log.info("루틴 영상 정보 : {}", routineVideoList);

        // 2. 루틴 영상 정보를 통해 실제 영상 정보 조회
        for (RoutineVideo routineVideo : routineVideoList) {
            // 현재 루틴 영상이 유튜브 영상이라면
            if (routineVideo.getYoutubeVideoId() != null && routineVideo.getYoutubeVideoId() > 0) {
                YoutubeVideo youtubeVideo = postDao.getYoutubeById(routineVideo.getYoutubeVideoId());
                if (youtubeVideo != null) {
                    videoRoutineSessionData.getYoutubeVideoList().add(youtubeVideo);
                } else {
                    throw new IllegalStateException("해당 유튜브 영상 ID(" + routineVideo.getYoutubeVideoId() + ")에 대한 정보가 존재하지 않습니다.");
                }
            }

            // 현재 루틴 영상이 업로드 영상이라면
            else if (routineVideo.getUploadedVideoId() != null && routineVideo.getUploadedVideoId() > 0) {
                UploadedVideo uploadedVideo = postDao.getUploadById(routineVideo.getUploadedVideoId());
                if (uploadedVideo != null) {
                    videoRoutineSessionData.getUploadVideoList().add(uploadedVideo);
                } else {
                    throw new IllegalStateException("해당 업로드 영상 ID(" + routineVideo.getUploadedVideoId() + ")에 대한 정보가 존재하지 않습니다.");
                }
            }
        }

        // 정상적인 경우 리스트로 반환
        return List.of(videoRoutineSessionData);
    }

    // 게시글 정보 전달 후 게시글 아이디 반환
    @Override
    public Long postRoutine(PostDetailInfo postDetailInfo, String token) {

        int result = 0;

        //유저 아이디 반환
        Long userId = authUtil.extractUserIdFromToken(token);

        // 1. 게시글 정보 삽입(썸네일 url 나중에 저장)
        Post postInfo = new Post();
        postInfo.setTitle(postDetailInfo.getTitle()); //제목
        postInfo.setContent(postDetailInfo.getContent()); //내용
        postInfo.setUserId(userId); //유저 아이디
        postInfo.setRoutineId(postDetailInfo.getRoutineId()); //루틴 아이디

        //게시글 등록 하고 게시글 ID 반환받기
        result = postDao.postRoutine(postInfo);

        //루틴 공유 상태 true로 변경
        postDao.postRoutineShared(postDetailInfo.getRoutineId());

        // 2. 썸네일 저장
        if (postDetailInfo.getThumbnail() != null && !postDetailInfo.getThumbnail().isEmpty()) {
            try {
                String thumbnailUrl  = s3Uploader.upload(postDetailInfo.getThumbnail(), "post-thumbnail");
                postInfo.setThumbnailUrl(thumbnailUrl);

                //post 테이블 썸네일 url 등록
                result = postDao.postRoutinethumbnailUrl(postInfo);

                //파일 테이블에 등록할 값 세팅
                PostFile postFile = new PostFile();
                postFile.setFileUrl(thumbnailUrl); //파일 URL
                postFile.setOriginalFilename(postDetailInfo.getThumbnail().getOriginalFilename()); //파일 원본 이름
                postFile.setFileType("THUMBNAIL_IMAGE"); //파일 타입
                postFile.setPostId(postInfo.getPostId()); //게시글 아이디

                //게시글 파일 저장
                result = postDao.postDaoFileInsert(postFile);

            } catch (IOException e) {
                throw new RuntimeException("썸네일 업로드 실패: " + e.getMessage(), e);
            }
        }

        // 3. 게시글 이미지/영상 저장
        for (MultipartFile file : postDetailInfo.getFiles()) {
            if (file.isEmpty()) continue; //첨부파일이 없으면 실행 안함

            try { //게시글 첨부파일 올리기
                String fileUrl = s3Uploader.upload(file, "post-files");
                PostFile postFile = new PostFile();

                //파일 테이블에 올릴 것
                postFile.setFileUrl(fileUrl); //파일 url
                postFile.setOriginalFilename(file.getOriginalFilename()); //파일 원본 이름
                postFile.setPostId(postInfo.getPostId()); //게시글 아이디

                //비디오인지 이미지인지 타입 정해야함
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("video")) {
                    postFile.setFileType("POST_VIDEO");
                } else {
                    postFile.setFileType("POST_IMAGE");
                }

                //파일 테이블에 등록!!
                result = postDao.postDaoFileInsert(postFile);

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
            }
        }

        return postInfo.getPostId();
    }

    // 게시글 상세 정보 가져오기
    @Override
    public Post getPost(Long postId) {

        return postDao.getPost(postId);
    }

    // 게시글 파일 정보 가져오기
    @Override
    public List<PostFile> getFiles(Long postId) {
        return postDao.getFiles(postId);
    }

    // 루틴 정보 가져오기
    @Override
    public Routine getRoutine(Long routineId) {
        return postDao.getRoutine(routineId);
    }

    //게시글 작성한 유저 정보 반환
    @Override
    public User getPostUser(Long postId) {
        return postDao.getPostUser(postId);
    }
    
    //게시글 삭제
    @Transactional
    @Override
    public int deletePost(Long postId) {

        //삭제할 게시글 파일 url 가져오기
        List<String> fileUrls = postDao.selectFileUrlsByPostId(postId);
        
        //s3에서 게시글에 해당하는 파일들 삭제
        for (String fileUrl : fileUrls) {
            s3Uploader.delete(fileUrl);
        }
        
        //db에서 게시글 삭제
        return postDao.deletePost(postId);
    }

    //게시글 전체 목록 저허;
    @Override
    public List<Post> listPosts() {
        return postDao.listPosts();
    }

    // userId로 본인이 작성한 게시글 목록 가져오기
    @Override
    public List<Post> getPostList(Long userId) {
        return postDao.selectPostList(userId);
    }

    //게시글 최신순 조회
    @Override
    public List<Post> getLatestPosts() {
        return postDao.getLatestPosts();
    }

    //게시글 인기순 조회
    @Override
    public List<Post> getPopularPosts() {
        return postDao.getPopularPosts();
    }

    //게시글 운동 부위별 조회
    @Override
    public List<Post> getPostsByPart(String part) {
        return postDao.getPostsByPart(part);
    }

    //게시글 수정
    @Transactional
    @Override
    public void updatePost(Long postId, PostDetailInfo postDetailInfo, String token) {

        Long userId = authUtil.extractUserIdFromToken(token);

        // 1. 기존 게시글 확인 및 권한 검사
        Post existingPost = postDao.getPost(postId);
        if (existingPost == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        if (!existingPost.getUserId().equals(userId)) {
            throw new SecurityException("게시글 수정 권한이 없습니다.");
        }

        // 2. 게시글 본문 업데이트
        Post post = new Post();
        post.setPostId(postId);
        post.setTitle(postDetailInfo.getTitle());
        post.setContent(postDetailInfo.getContent());
        post.setUserId(userId);
        postDao.updatePost(post);

        // 3. 삭제할 파일 처리
        List<Long> filesToDelete = postDetailInfo.getFilesToDelete();
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            List<PostFile> existingFiles = postDao.getFiles(postId);
            for (PostFile file : existingFiles) {
                log.info("삭제할 파일 : {}", file);
                if (filesToDelete.contains(file.getPostFileId())) {
                    if (file.getFileUrl() != null) {
                        s3Uploader.delete(file.getFileUrl()); // S3에서 삭제
                    }
                    postDao.deleteFileById(file.getPostFileId()); // DB에서 삭제
                }
            }
        }

        // 4. 새 파일 업로드 및 저장
        List<MultipartFile> newFiles = postDetailInfo.getFiles();
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    try {
                        String fileUrl = s3Uploader.upload(file, "post-files");

                        PostFile postFile = new PostFile();
                        postFile.setPostId(postId);
                        postFile.setOriginalFilename(file.getOriginalFilename());
                        postFile.setFileUrl(fileUrl);

                        String contentType = file.getContentType();
                        if (contentType != null && contentType.startsWith("video")) {
                            postFile.setFileType("POST_VIDEO");
                        } else {
                            postFile.setFileType("POST_IMAGE");
                        }

                        postDao.insertFile(postFile);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 실패", e);
                    }
                }
            }
        }

        // 5. 썸네일 이미지 새로 업로드한 경우
        MultipartFile newThumbnail = postDetailInfo.getThumbnail();
        if (newThumbnail != null && !newThumbnail.isEmpty()) {
            try {
                String thumbnailUrl = s3Uploader.upload(newThumbnail, "post-thumbnail");
                post.setThumbnailUrl(thumbnailUrl);
                postDao.postRoutinethumbnailUrl(post); // 게시글 썸네일 URL 업데이트

                PostFile thumbnailFile = new PostFile();
                thumbnailFile.setFileUrl(thumbnailUrl);
                thumbnailFile.setOriginalFilename(newThumbnail.getOriginalFilename());
                thumbnailFile.setFileType("THUMBNAIL_IMAGE");
                thumbnailFile.setPostId(postId);
                postDao.postDaoFileInsert(thumbnailFile); // 썸네일 파일 DB 저장
            } catch (IOException e) {
                throw new RuntimeException("썸네일 업로드 실패", e);
            }
        }
    }

    // 게시글 좋아요를 누르거나 취소(좋아요 상태 변경)
    @Override
    public LikeResponse toggleLike(Long postId, String token) {

        //유저 아이디 반환
        Long userId = authUtil.extractUserIdFromToken(token);

        //이미 해당 게시글에 좋아요를 눌렀는지 확인
        boolean alreadyLiked = postDao.existsByPostIdAndUserId(postId, userId);

        if (alreadyLiked) {
            // 이미 좋아요를 눌렀다면 좋아요 취소
            postDao.deleteLike(postId, userId);
        } else {
            // 좋아요 객체 생성해서 전달
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postDao.insertLike(postLike); //  PostLike 객체 전달
        }

        // 게시글의 총 좋아요 수 조회
        int likeCount = postDao.countLikes(postId);

        //(좋아요 수 + 현재 상태 반영)
        return new LikeResponse(likeCount, !alreadyLiked);
    }

    // 게시글의 좋아요 수 및 내가 좋아요 눌렀는지 여부 조회(내가 지금 좋아요를 눌렀는지, 총 좋아요 수)
    @Override
    public LikeResponse getLikeInfo(Long postId, String token) {

        //유저 아이디 반환
        Long userId = authUtil.extractUserIdFromToken(token);

        boolean likedByMe = postDao.existsByPostIdAndUserId(postId, userId);
        int likeCount = postDao.countLikes(postId);

        return new LikeResponse(likeCount, likedByMe);
    }

    // 게시글 상세페이지 조회 시 조회수 1 증가
    @Override
    public int increaseViewCount(Long postId) {
        postDao.increaseViewCount(postId); //조회수 증가

        return postDao.viewCount(postId); //조회수 리턴
    }
    
    //게시글 댓글 수 반환
    @Override
    public int getCommentCount(Long postId) {
        return postDao.getCommentCount(postId);
    }


}



