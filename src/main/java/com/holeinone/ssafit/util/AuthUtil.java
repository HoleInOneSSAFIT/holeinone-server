package com.holeinone.ssafit.util;

import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.model.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    private final UserService userService;

    public AuthUtil(UserService userService) {
        this.userService = userService;
    }

    public User extractUserFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            return userService.getInfo(token);
        } catch (Exception e) {
            // 예외 발생 시 null 반환 또는 커스텀 예외 처리
            return null;
        }
    }

    public Long extractUserIdFromToken(String token) {
        User user = extractUserFromToken(token);
        if (user == null) {
            throw new AccessDeniedException("Invalid token or user not found");
        }
        return user.getUserId();
    }

}
