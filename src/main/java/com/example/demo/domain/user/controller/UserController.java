package com.example.demo.domain.user.controller;

import com.example.demo.common.security.dto.RegisterRequest;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 사용자 등록
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        
        log.debug("사용자 등록 요청: {}", request.getUsername());
        
        // 중복 사용자 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("이미 존재하는 사용자: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("이미 존재하는 사용자입니다."));
        }
        
        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .enabled(true)
                .build();
        
        userRepository.save(user);
        
        log.info("사용자 등록 성공: {}", request.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "사용자 등록이 완료되었습니다.");
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        
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
