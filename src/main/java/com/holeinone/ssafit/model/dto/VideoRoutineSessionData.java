package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//세션 저장용 DTO
@Data
@NoArgsConstructor
public class VideoRoutineSessionData {

    private List<YoutubeVideo> youtubeVideoList  = new ArrayList<>();
    private List<UploadedVideo> uploadVideoList  = new ArrayList<>();

}
