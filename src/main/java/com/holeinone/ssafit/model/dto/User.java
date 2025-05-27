package com.holeinone.ssafit.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String username; // 아이디
    private String password; // 비밀번호
    private String name;
    private String nickname;
    private String profileImage; // 프로필 사진 경로
    private String gender;
    private LocalDate birthdate;
    private Double height; // 선택사항
    private Double weight; // 선택사항
    private Date joinedAt;
    private Boolean isActive; // 탈퇴여부 - false면 부정상태
    private String role;
    private String refreshToken;
}
