package com.holeinone.ssafit.model.dao;

import com.holeinone.ssafit.model.dto.User;
import org.apache.ibatis.annotations.Param;

public interface UserDao {
    int save(User user);
    User findByUsername(@Param("username") String username);
    void updateRefreshToken(@Param("username") String username, @Param("refreshToken") String refreshToken);
}
