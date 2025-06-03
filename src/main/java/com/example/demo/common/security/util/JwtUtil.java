package com.example.demo.common.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    
    /**
     * Access Token 생성
     */
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("토큰에서 사용자명 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰에서 역할 추출
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("토큰에서 역할 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            log.error("토큰 타입 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 만료 시간 추출
     */
    public Date getExpirationFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("토큰 만료시간 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException e) {
            log.error("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT 형식이 올바르지 않습니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT가 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 클레임이 비어있습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Access Token인지 확인
     */
    public boolean isAccessToken(String token) {
        String type = getTokenType(token);
        return "access".equals(type);
    }
    
    /**
     * Refresh Token인지 확인
     */
    public boolean isRefreshToken(String token) {
        String type = getTokenType(token);
        return "refresh".equals(type);
    }
    
    /**
     * 토큰 파싱
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
