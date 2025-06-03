package com.example.demo.common.security.filter;

import com.example.demo.common.security.service.RedisService;
import com.example.demo.common.security.util.JwtUtil;
import com.example.demo.domain.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("JWT 필터 실행 - {} {}", method, requestURI);
        
        String token = resolveToken(request);
        
        if (token != null) {
            log.debug("JWT 토큰 발견: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            
            // 블랙리스트 확인
            if (redisService.isBlacklisted(token)) {
                log.warn("블랙리스트된 토큰입니다.");
                filterChain.doFilter(request, response);
                return;
            }
            
            // 토큰 유효성 검증
            if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                if (username != null && role != null) {
                    log.debug("인증 성공 - 사용자: {}, 역할: {}", username, role);
                    
                    // Spring Security 인증 정보 설정
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            username, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("SecurityContext에 인증 정보 설정 완료 - 사용자: {}", username);
                } else {
                    log.warn("토큰에서 사용자 정보를 추출할 수 없습니다.");
                }
            } else {
                log.warn("유효하지 않은 토큰이거나 Access Token이 아닙니다.");
            }
        } else {
            log.debug("JWT 토큰이 없습니다 - {} {}", method, requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization 헤더: {}", bearerToken != null ? bearerToken.substring(0, Math.min(bearerToken.length(), 30)) + "..." : "null");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("토큰 추출 성공: {}...", token.substring(0, Math.min(token.length(), 20)));
            return token;
        }
        
        log.debug("유효한 Bearer 토큰이 없습니다.");
        return null;
    }
}
