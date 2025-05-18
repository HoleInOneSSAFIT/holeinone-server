package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.UserDao;
import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.security.JwtUtil;
import java.nio.file.AccessDeniedException;
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

        // isActive로 탈퇴한 사람은 로그인 못하게 조건 걸어주기
        if(!user.getIsActive()) {
            throw new RuntimeException("이미 탈퇴한 계정입니다.");
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

    @Override
    public void logout(String refreshToken) {
        userDao.deleteRefreshToken(refreshToken);
    }

    @Override
    public Map<String, String> rotate(String rawRefreshToken) {
        // 쿠키에서 바로 꺼낸 rawRefreshToken이 db에 있는지부터 확인
        User user = userDao.findByRefreshToken(rawRefreshToken);
        if(user == null) throw new RuntimeException("토큰이 존재하지 않습니다.");

        // 새 토큰 발급하기
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // rotate하기: 기존의 토큰은 null로 하고, 새로운 토큰 업데이트 하기
        userDao.updateRefreshToken(user.getUsername(), newRefreshToken);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    @Override
    public void update(User user, String token) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token);

        // user에 username 강제로 설정함(프론트에서 username 넘겨주지 않아도 됨)
        user.setUsername(username);

//        if(!username.equals(user.getUsername())) {
//            throw new AccessDeniedException("본인만 수정할 수 있습니다.");
//        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.update(user);
    }

    @Override
    public void deleteAccount(String token) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token);
        User user = userDao.findByUsername(username);

        if(user == null) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }

        user.setIsActive(false); // soft delete
        userDao.updateState(user);
    }
}
