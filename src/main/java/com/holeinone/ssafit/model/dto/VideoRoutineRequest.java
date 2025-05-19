package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//운동 영상 루틴들 저장
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoutineRequest {

    private List<YoutubeVideo> youtubeVideos;
    private List<UploadedVideo> uploadedVideos;

}
