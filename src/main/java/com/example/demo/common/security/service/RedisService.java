package com.example.demo.common.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    /**
     * Refresh Token 저장
     */
    public void saveRefreshToken(String username, String refreshToken, long expiration) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("Refresh Token 저장 완료 - 사용자: {}", username);
        } catch (Exception e) {
            log.error("Refresh Token 저장 실패 - 사용자: {}, 오류: {}", username, e.getMessage());
        }
    }
    
    /**
     * Access Token 저장
     */
    public void saveAccessToken(String username, String accessToken, long expiration) {
        String key = ACCESS_TOKEN_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("Access Token 저장 완료 - 사용자: {}", username);
        } catch (Exception e) {
            log.error("Access Token 저장 실패 - 사용자: {}, 오류: {}", username, e.getMessage());
        }
    }
    
    /**
     * Refresh Token 조회
     */
    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            String token = redisTemplate.opsForValue().get(key);
            log.debug("Refresh Token 조회 - 사용자: {}, 존재여부: {}", username, token != null);
            return token;
        } catch (Exception e) {
            log.error("Refresh Token 조회 실패 - 사용자: {}, 오류: {}", username, e.getMessage());
            return null;
        }
    }
    
    /**
     * Refresh Token 삭제
     */
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Refresh Token 삭제 - 사용자: {}, 삭제됨: {}", username, deleted);
        } catch (Exception e) {
            log.error("Refresh Token 삭제 실패 - 사용자: {}, 오류: {}", username, e.getMessage());
        }
    }
    
    /**
     * Access Token을 블랙리스트에 추가
     */
    public void addToBlacklist(String accessToken, long expiration) {
        String key = BLACKLIST_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "logout", expiration, TimeUnit.MILLISECONDS);
            log.debug("Access Token 블랙리스트 추가 완료");
        } catch (Exception e) {
            log.error("Access Token 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }
    
    /**
     * Access Token이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("Access Token 블랙리스트 확인 - 블랙리스트됨: {}", exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Access Token 블랙리스트 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = getRefreshToken(username);
        boolean isValid = storedToken != null && storedToken.equals(refreshToken);
        log.debug("Refresh Token 검증 - 사용자: {}, 유효함: {}", username, isValid);
        return isValid;
    }
}
