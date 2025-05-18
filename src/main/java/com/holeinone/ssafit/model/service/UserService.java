package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.User;
import java.util.Map;

public interface UserService {
    void register(User user);
    Map<String, String> login(String username, String password);
    void logout(String refreshToken);
}
