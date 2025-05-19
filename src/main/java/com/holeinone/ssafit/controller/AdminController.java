package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.model.service.UserService;
import com.holeinone.ssafit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 모든 회원 조회
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String auth) {
        checkAdmin(auth);

        return ResponseEntity.ok(userService.getAllUsers());
    }

    // admin인지 체크
    private void checkAdmin(String header) throws AccessDeniedException {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AccessDeniedException("헤더가 존재하지 않습니다.");
        }

        // 파싱
        String token = header.substring(7);

        // jwtUtil에서 role 추출
        String role = jwtUtil.extractUserRole(token);

        if (!role.equals("ADMIN")) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }
}
