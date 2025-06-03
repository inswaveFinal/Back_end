package com.example.demo.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {
        
        log.error("로그인 실패: {}", exception.getMessage());
        
        String errorMessage = "로그인에 실패했습니다.";
        
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 사용자입니다.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "비활성화된 계정입니다.";
        } else if (exception instanceof LockedException) {
            errorMessage = "잠긴 계정입니다.";
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", errorMessage);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        log.debug("로그인 실패 응답 전송 완료");
    }
}
