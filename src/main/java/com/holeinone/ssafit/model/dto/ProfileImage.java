package com.holeinone.ssafit.model.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileImage {
    private Long profileImageId;
    private String s3Url; // S3에 저장한 경로
    private String originalImageName;
    private String imageType;
    private Date uploadedAt;
    private Long userId;
}
