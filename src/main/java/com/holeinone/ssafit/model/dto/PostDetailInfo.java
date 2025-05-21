package com.holeinone.ssafit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

//게시글 상세 페이지 정보
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailInfo {

    private String title;
    private String content;
    private Long routineId;
    private Long userId;
    private String thumbnail_url;

    // 썸네일 1개
    private MultipartFile thumbnail;

    // 게시글 첨부파일 여러 개
    private List<MultipartFile> files = new ArrayList<>();

    //루틴 영상 리스트
    private List<VideoRoutineSessionData> videoRoutineSessionDatas = new ArrayList<>();


}
