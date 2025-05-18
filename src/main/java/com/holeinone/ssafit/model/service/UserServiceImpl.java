package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dao.UserDao;
import com.holeinone.ssafit.model.dto.User;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

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
}
