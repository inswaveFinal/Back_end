package com.example.demo.common.config;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        
        // 테스트 사용자가 없으면 생성
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .enabled(true)
                    .build();
            
            userRepository.save(admin);
            log.info("관리자 계정 생성 완료: admin/admin123");
        }
        
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role("ROLE_USER")
                    .enabled(true)
                    .build();
            
            userRepository.save(user);
            log.info("사용자 계정 생성 완료: user/user123");
        }
    }
}
