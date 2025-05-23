package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.dto.Post;
import com.holeinone.ssafit.model.dto.User;
import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final PostService postService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // 필수 항목 누락 체크
        if (user.getProfileImage() == null || user.getUsername() == null || user.getPassword() == null
                || user.getName() == null || user.getNickname() == null || user.getGender() == null || user.getBirthdate() == null) {
            throw new IllegalArgumentException("필수 항목 입력이 누락되었습니다.");
        }

        userService.register(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 아이디 중복확인
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean isDuplicate = userService.isUsernameExist(username);
        return ResponseEntity.ok(isDuplicate); // true: 이미 있다, false: 없다
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isDuplicateNickname = userService.isNicknameExist(nickname);
        return ResponseEntity.ok(isDuplicateNickname); // true: 이미 있다, false: 없다
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest, HttpServletResponse response) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, String> tokens = userService.login(username, password);

        // refreshToken은 HttpOnly 쿠키, accessToken은 JSON
        ResponseCookie cookie = ResponseCookie.from("refreshToken",
                        tokens.get("refreshToken"))
                .httpOnly(true).secure(false)
                .sameSite("Lax").path("/")
                .maxAge(7 * 24 * 60 * 60).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", tokens.get("accessToken")));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 refresh token 추출하기
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        // DB에서 추출한 refresh token 삭제
        if (refreshToken != null && !refreshToken.isBlank()) {
            userService.logout(refreshToken);
        }

        // 브라우저 쿠키 제거
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)  // 즉시 만료
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("로그아웃 되었습니다");
    }

    // 토큰 만료시 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(value="refreshToken", required=false) String refreshToken,
                                                HttpServletResponse response) {

        if(refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("리프레시 토큰이 존재하지 않습니다.");
        } // 다시 로그인 하도록 유도하기

        Map<String, String> tokens = userService.rotate(refreshToken); // 기존의 리프레시 토큰은 사용XX

        // 새로운 리프레시 토큰 받아와서 쿠키 교체하기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true).secure(false).sameSite("Lax")
                .path("/").maxAge(7 * 24 * 60 * 60).build(); // 새로운 리프레시 쿠키 세팅하기
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 새로운 액세스 토큰 받아서 헤더에 넣기
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                .build();
    }

    // 사용자 본인이 작성한 게시글 목록
    @GetMapping("/postlist")
    public ResponseEntity<List<Post>> getPostList(@RequestHeader("Authorization") String token) throws AccessDeniedException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userService.getInfo(token);
        Long userId = user.getUserId(); // token에서 가져온 정보에서 userId 추출

        List<Post> postList = postService.getPostList(userId); // userId에 해당하는 게시글 목록 가져오기

        return ResponseEntity.ok(postList);
    }

    // 사용자가 작성한 댓글 목록
    // 사용자가 좋아요 한 게시글 목록

    // 정보 보여주기 - info page
    @GetMapping("/info")
    public ResponseEntity<User> getInfo(@RequestHeader("Authorization") String token) throws AccessDeniedException {
        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = userService.getInfo(token);

        return ResponseEntity.ok(user);
    }

    // 정보 수정
    @PutMapping("/update")
    public ResponseEntity<String> update(@RequestBody User user, @RequestHeader("Authorization") String token)
            throws AccessDeniedException {

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("비밀번호는 필수입니다.");  // 400
        }

        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        userService.update(user, token);

        return ResponseEntity.ok("수정이 완료되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String token)
            throws AccessDeniedException {
        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        userService.deleteAccount(token);

        return ResponseEntity.ok("계정이 성공적으로 탈퇴 처리 되었습니다.");
    }

}
