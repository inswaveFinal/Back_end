package com.example.demo.common.security.service;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 조회 시도: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });
        
        log.debug("사용자 조회 성공: {}", username);
        return user;
    }
}
