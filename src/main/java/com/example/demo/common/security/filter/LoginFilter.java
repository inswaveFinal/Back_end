package com.example.demo.common.security.filter;

import com.example.demo.common.security.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public LoginFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/loginPro");
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws AuthenticationException {
        
        log.debug("로그인 시도 - URL: {}, Method: {}", request.getRequestURL(), request.getMethod());
        
        if (!request.getMethod().equals("POST")) {
            throw new IllegalArgumentException("POST 방식만 지원됩니다.");
        }
        
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            
            log.debug("로그인 요청 파싱 완료 - 사용자: {}", loginRequest.getUsername());
            
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                );
            
            return getAuthenticationManager().authenticate(authToken);
            
        } catch (IOException e) {
            log.error("로그인 요청 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("로그인 요청을 파싱할 수 없습니다.", e);
        }
    }
}
