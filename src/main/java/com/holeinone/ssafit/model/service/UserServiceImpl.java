package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.UserDao;
import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.security.JwtUtil;
import com.holeinone.ssafit.util.S3Uploader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Uploader s3Uploader;

    @Transactional
    @Override
    public void register(User user, MultipartFile img) throws IOException {

        if (isUsernameExist(user.getUsername()))
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");

        // 프로필 사진 - S3 업로드
        if (img != null && !img.isEmpty()) {
            String url = s3Uploader.upload(img, "profile-images");
            user.setProfileImage(url);
        }

        // 나머지 정보
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        user.setJoinedAt(new Date());

        userDao.save(user);
    }

    @Override
    public Boolean isUsernameExist(String username) {
        return userDao.findByUsername(username) != null; // 이미 있다면 true
    }

    @Override
    public Boolean isNicknameExist(String nickname) {
        return userDao.findByNickname(nickname) != null; // 이미 있다면 true
    }

    @Override
    public Map<String, String> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 입력해주세요.");
        }

        User user = userDao.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        // isActive가 false면 탈퇴한 상태
        if(!user.getIsActive()) {
            throw new IllegalStateException("이미 탈퇴한 계정입니다.");
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // DB에 리프레시 토큰 저장
        userDao.updateRefreshToken(user.getUsername(), refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

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
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getUserId(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // rotate하기: 기존의 토큰은 null로 하고, 새로운 토큰 업데이트 하기
        userDao.updateRefreshToken(user.getUsername(), newRefreshToken);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    @Override
    public User getInfo(String token) {
        String name = jwtUtil.extractUsername(token);

        return userDao.findByUsername(name);
    }

    @Override
    public void update(User user, String token) {
        String username = jwtUtil.extractUsername(token);

        // user에 username 강제로 설정함(프론트에서 username 넘겨주지 않아도 됨)
        user.setUsername(username);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.update(user);
    }

    @Override
    public void deleteAccount(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userDao.findByUsername(username);

        if(user == null) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }

        user.setIsActive(false); // soft delete
        userDao.updateState(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public void deleteAccountByUsername(String username) {
        User user = userDao.findByUsername(username);

        if(user == null) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }

        user.setIsActive(false);
        userDao.updateState(user);
    }
}
