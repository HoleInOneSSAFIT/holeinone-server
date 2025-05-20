package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao {
    void save(User user);
    User findByUsername(@Param("username") String username);
    void updateRefreshToken(@Param("username") String username, @Param("refreshToken") String refreshToken);
    void deleteRefreshToken(String refreshToken);
    User findByRefreshToken(String refreshToken);
    void update(User user);
    void updateState(User user); // soft delete를 위한 is_active 상태 업데이트
    List<User> findAllUsers();
}
