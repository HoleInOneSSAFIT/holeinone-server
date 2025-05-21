package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.exception.CustomException;
import com.holeinone.ssafit.model.dao.PostDao;
import com.holeinone.ssafit.model.dao.VideoDao;
import com.holeinone.ssafit.model.dto.RoutineVideo;
import com.holeinone.ssafit.model.dto.UploadedVideo;
import com.holeinone.ssafit.model.dto.VideoRoutineSessionData;
import com.holeinone.ssafit.model.dto.YoutubeVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PostServiceImpl implements PostService{

    private final PostDao postDao;

    public PostServiceImpl(PostDao postDao) {
        this.postDao = postDao;
    }

    //루틴 아이디를 통해 루틴 영상 정보 조회
    @Override
    public List<VideoRoutineSessionData> getRoutineById(Long routineId) {

        // 1. 루틴 아이디를 통해 루틴 영상 정보 조회
        List<RoutineVideo> routineVideoList = postDao.getRoutineById(routineId);

        // 루틴이 존재하지 않을 경우 예외 발생
        if (routineVideoList == null || routineVideoList.isEmpty()) {
            throw new CustomException("해당 루틴 ID(" + routineId + ")에 대한 영상 정보가 존재하지 않습니다.");
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
                    throw new CustomException("해당 유튜브 영상 ID(" + routineVideo.getYoutubeVideoId() + ")에 대한 정보가 존재하지 않습니다.");
                }
            }

            // 현재 루틴 영상이 업로드 영상이라면
            else if (routineVideo.getUploadedVideoId() != null && routineVideo.getUploadedVideoId() > 0) {
                UploadedVideo uploadedVideo = postDao.getUploadById(routineVideo.getUploadedVideoId());
                if (uploadedVideo != null) {
                    videoRoutineSessionData.getUploadVideoList().add(uploadedVideo);
                } else {
                    throw new CustomException("해당 업로드 영상 ID(" + routineVideo.getUploadedVideoId() + ")에 대한 정보가 존재하지 않습니다.");
                }
            }
        }

        // 정상적인 경우 리스트로 반환
        return List.of(videoRoutineSessionData);
    }
}
