package com.holeinone.ssafit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 모든 권한 허용으로 true 상태, 권한에 따라 접근 막고 싶다면 false 처리 하고 아래 matchers 주석 해제
    // application.properties도 false 처리 해야함
    @Value("${security.open-all:true}")
    private boolean openAll;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**" // 기능별 접근 권한 추가하기
    };

    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    if (openAll) { // 개발용 - 전체허용
                        auth.anyRequest().permitAll();
                    } else {
                        auth.requestMatchers(PUBLIC_URLS).permitAll() // 일단 전체 허용한 상태
//                                .requestMatchers(HttpMethod.GET, "/api/videos/**").permitAll()
//                                .requestMatchers("/api/videos/**").hasAnyRole("ROLE_USER", "ROLE_ADMIN")
//                                .requestMatchers("/api/admin/**").hasRole("ROLE_ADMIN")
                                .anyRequest().authenticated();
                    }
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 들어오는 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
