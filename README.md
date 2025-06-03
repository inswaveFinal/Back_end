# Spring Security JWT 인증 시스템

Spring Security와 JWT를 활용한 완전한 인증 시스템입니다.

## 주요 기능

- ✅ Spring Security 기반 인증/인가
- ✅ JWT Access Token (3일) 및 Refresh Token (7일)
- ✅ Redis를 이용한 토큰 저장 및 블랙리스트 관리
- ✅ 로그인/로그아웃 처리
- ✅ 토큰 갱신 기능
- ✅ 사용자 등록 기능

## 기술 스택

- **Backend**: Spring Boot 3.3.12, Spring Security 6
- **Database**: MySQL 8.0 (개발), H2 (테스트)
- **Cache**: Redis 7
- **JWT**: JJWT 0.12.3
- **Build Tool**: Gradle 8.14
- **Java**: 17

## 빠른 시작

### 1. Redis 서버 실행 (Docker 사용)

```bash
# Docker Compose로 Redis 실행 (추천)
docker-compose up -d redis

# 또는 Redis만 간단히 실행
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

**Redis 관리 도구 (선택사항):**
```bash
# Redis Commander 웹 UI 실행
docker-compose up -d redis-commander
# 브라우저에서 http://localhost:8081 접속
```

### 2. 데이터베이스 설정

MySQL 데이터베이스 `demo_db`를 생성하거나 application.properties에서 설정을 변경하세요.

### 3. 애플리케이션 실행

```bash
# 의존성 설치 및 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

서버는 `http://localhost:8090`에서 실행됩니다.

## API 사용법

### 기본 테스트 계정

- **관리자**: `admin` / `admin123`
- **사용자**: `user` / `user123`

### 주요 엔드포인트

| 메서드 | 경로 | 설명 | 인증 필요 |
|--------|------|------|-----------|
| POST | `/auth/register` | 사용자 등록 | ❌ |
| POST | `/loginPro` | 로그인 | ❌ |
| POST | `/auth/refresh` | 토큰 갱신 | ❌ |
| POST | `/auth/logout` | 로그아웃 | ✅ |
| GET | `/auth/me` | 현재 사용자 정보 | ✅ |

### 사용 예시

```bash
# 1. 로그인
curl -X POST http://localhost:8090/loginPro \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 응답 예시
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN"
}

# 2. 인증이 필요한 API 호출
curl -X GET http://localhost:8090/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 3. 로그아웃
curl -X POST http://localhost:8090/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Docker 명령어

### Redis 관련

```bash
# Redis 시작
docker-compose up -d redis

# Redis 중지
docker-compose stop redis

# Redis 로그 확인
docker-compose logs redis

# Redis CLI 접속
docker exec -it spring-security-redis redis-cli

# Redis 데이터 초기화
docker-compose down redis
docker volume rm back_end_redis_data
docker-compose up -d redis
```

### 전체 서비스 관리

```bash
# 모든 서비스 시작
docker-compose up -d

# 모든 서비스 중지
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── common/
│   ├── config/
│   │   └── DataInitializer.java          # 초기 데이터 생성
│   └── security/
│       ├── config/
│       │   ├── RedisConfig.java           # Redis 설정
│       │   └── SecurityConfig.java        # Spring Security 설정
│       ├── controller/
│       │   └── AuthController.java        # 인증 관련 API
│       ├── dto/
│       │   ├── LoginRequest.java          # 로그인 요청 DTO
│       │   ├── LoginResponse.java         # 로그인 응답 DTO
│       │   └── RefreshTokenRequest.java   # 토큰 갱신 요청 DTO
│       ├── filter/
│       │   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│       │   └── LoginFilter.java           # 로그인 처리 필터
│       ├── handler/
│       │   ├── LoginFailureHandler.java   # 로그인 실패 핸들러
│       │   └── LoginSuccessHandler.java   # 로그인 성공 핸들러
│       ├── service/
│       │   ├── CustomUserDetailsService.java # 사용자 인증 서비스
│       │   └── RedisService.java          # Redis 관련 서비스
│       └── util/
│           └── JwtUtil.java               # JWT 유틸리티
└── domain/
    └── user/
        ├── controller/
        │   └── UserController.java        # 사용자 관련 API
        ├── entity/
        │   └── User.java                  # 사용자 엔티티
        └── repository/
            └── UserRepository.java        # 사용자 레포지토리
```

## 보안 고려사항

- **JWT Secret**: 실제 운영 환경에서는 환경 변수로 관리
- **HTTPS**: 실제 운영 환경에서는 HTTPS 사용 필수
- **CORS**: 필요에 따라 CORS 정책 조정
- **Rate Limiting**: API 호출 제한 고려
- **로그 관리**: 민감한 정보 로깅 방지

## 트러블슈팅

### Redis 연결 오류
```bash
# Redis 컨테이너 상태 확인
docker ps | grep redis

# Redis 로그 확인
docker logs spring-security-redis

# Redis 재시작
docker restart spring-security-redis
```

### 데이터베이스 연결 오류
- MySQL 서버가 실행 중인지 확인
- `application.properties`의 데이터베이스 설정 확인
- 데이터베이스 및 사용자 권한 확인

### 포트 충돌
- 6379 (Redis), 8090 (애플리케이션), 3306 (MySQL) 포트 사용 여부 확인

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
