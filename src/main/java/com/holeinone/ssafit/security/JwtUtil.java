package com.holeinone.ssafit.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretBase64;

    private static final long ACCESS_MS  = 1000 * 60 * 20 ; // 20분
    private static final long REFRESH_MS = 7 * 24 * 60 * 60 * 1000;  // 7일

    // 내부 헬퍼 - Key 객체
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    // Access Token 생성
    public String generateAccessToken(String username, Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_MS))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_MS))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return claims(token).get("userId", Long.class);
    }

    public String extractUserRole(String token) {
        return claims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return !claims(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
