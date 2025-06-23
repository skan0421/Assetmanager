package com.assetmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * Phase 2.3: 테스트용 임시 설정 (Phase 3에서 본격 구현 예정)
 * 
 * 테스트 엔드포인트는 com.assetmanager.test.controller 패키지에 위치
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/test/**").permitAll()  // 테스트 API 허용
                .requestMatchers("/actuator/**").permitAll()  // Actuator 허용
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/test/**")  // 테스트 API CSRF 비활성화
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()  // H2 콘솔을 위한 설정
            )
            .httpBasic(httpBasic -> {});  // 기본 HTTP 인증 사용

        return http.build();
    }
}
