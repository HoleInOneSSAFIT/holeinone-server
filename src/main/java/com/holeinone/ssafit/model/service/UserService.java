package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.User;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

public interface UserService {
    void register(User user);
    Map<String, String> login(String username, String password);
    void logout(String refreshToken);
    Map<String, String> rotate(String refreshToken);
    void update(User user, String token) throws AccessDeniedException;
    void deleteAccount(String token) throws AccessDeniedException;
    List<User> getAllUsers();
}
