package com.example.demo.common.security.controller;

import com.example.demo.common.security.dto.LoginResponse;
import com.example.demo.common.security.dto.RefreshTokenRequest;
import com.example.demo.common.security.service.RedisService;
import com.example.demo.common.security.util.JwtUtil;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final UserRepository userRepository;
    
    // Access Token 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        log.debug("토큰 갱신 요청");
        
        // Refresh Token 유효성 검증
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.error("유효하지 않은 Refresh Token");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("유효하지 않은 Refresh Token입니다."));
        }
        
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        if (username == null) {
            log.error("Refresh Token에서 사용자명을 추출할 수 없음");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("유효하지 않은 Refresh Token입니다."));
        }
        
        // Redis에서 저장된 Refresh Token과 비교
        if (!redisService.validateRefreshToken(username, refreshToken)) {
            log.error("Redis에 저장된 Refresh Token과 일치하지 않음");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("유효하지 않은 Refresh Token입니다."));
        }
        
        // 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            log.error("사용자를 찾을 수 없음: {}", username);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("사용자를 찾을 수 없습니다."));
        }
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
        
        LoginResponse response = LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)  // 기존 Refresh Token 유지
                .tokenType("Bearer")
                .username(username)
                .role(user.getRole())
                .build();
        
        log.info("토큰 갱신 성공 - 사용자: {}", username);
        return ResponseEntity.ok(response);
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("유효하지 않은 토큰입니다."));
        }
        
        String accessToken = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(accessToken);
        
        if (username == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("유효하지 않은 토큰입니다."));
        }
        
        // Access Token을 블랙리스트에 추가
        Date expiration = jwtUtil.getExpirationFromToken(accessToken);
        if (expiration != null) {
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisService.addToBlacklist(accessToken, remainingTime);
            }
        }
        
        // Refresh Token 삭제
        redisService.deleteRefreshToken(username);
        
        // Security Context 초기화
        SecurityContextHolder.clearContext();
        
        log.info("로그아웃 성공 - 사용자: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃이 완료되었습니다.");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("인증되지 않은 사용자입니다."));
        }
        
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("사용자를 찾을 수 없습니다."));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("enabled", user.isEnabled());
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
