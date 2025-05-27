package com.holeinone.ssafit.model.dto;

import lombok.Data;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class UserJoinRequest {

    private String username;
    private String password;
    private String name;
    private String nickname;
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    private Double height;
    private Double weight;

    public User toEntity() {
        User u = new User();

        u.setUsername(username);
        u.setPassword(password);
        u.setName(name);
        u.setNickname(nickname);
        u.setGender(gender);
        u.setBirthdate(birthdate);
        u.setHeight(height);
        u.setWeight(weight);

        return u;
    }
}
