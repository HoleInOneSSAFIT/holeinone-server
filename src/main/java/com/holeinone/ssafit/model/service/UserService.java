package com.holeinone.ssafit.model.service;

import com.holeinone.ssafit.model.dto.User;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void register(User user, MultipartFile img) throws IOException;
    Map<String, String> login(String username, String password);
    Boolean isUsernameExist(String username);
    Boolean isNicknameExist(String nickname);
    void logout(String refreshToken);
    Map<String, String> rotate(String refreshToken);
    User getInfo(String token);
    void update(User user, String token) throws AccessDeniedException;
    void deleteAccount(String token) throws AccessDeniedException;
    List<User> getAllUsers();
    User getUserByUsername(String username);
    void deleteAccountByUsername(String username);
}
