package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.UserDao;
import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.security.JwtUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void register(User user) {
        // 비밀번호 해싱
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // role은 User로 고정 - 탈취 방지
        user.setRole("USER");

        user.setIsActive(true);
        user.setJoinedAt(new Date());

        userDao.save(user);
    }

    @Override
    public Map<String, String> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 입력해주세요.");
        }

        User user = userDao.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // DB에 리프레시 토큰 저장
        userDao.updateRefreshToken(user.getUsername(), refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        System.out.println("access_token = " + tokens.get("accessToken"));
        System.out.println("refresh_token = " + tokens.get("refreshToken"));

        return tokens;
    }
}
