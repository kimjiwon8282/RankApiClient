package com.example.RankCat.controller.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.RankCat.config.auth.TokenService;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.repository.UserRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
            // application.yml에 있는 변수들을 모두 가짜로라도 채워줘야 컨텍스트가 뜹니다.
            "MONGO_URI=mongodb://localhost:27017/test",
            "FASTAPI_URL=http://localhost:8000",
            "JWT_ISSUER=test",
            "JWT_SECRET_KEY=test_secret_key_at_least_32_characters_long",
            "GOOGLE_CLIENT_ID=test-id",
            "GOOGLE_CLIENT_SECRET=test-secret",
            "NAVER_CLIENT_ID=test-id",
            "NAVER_CLIENT_SECRET=test-secret",
            "KAKAO_CLIENT_ID=test-id",
            "MAIL_USERNAME=test@naver.com",
            "MAIL_PASSWORD=test-password",
            "cors.allowed-origins=http://localhost:3000,http://localhost:5173"
        })
@ActiveProfiles("local")
@Transactional
class RefreshTokenRotationTest {

    @Autowired private TokenService tokenService;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        testUser =
                userRepository.save(
                        User.builder()
                                .email("jiwon@pusan.ac.kr") // 지원님의 소중한 이메일
                                .password("pass123")
                                .nickname("랭캣개발자")
                                .build());
    }

    @Test
    @DisplayName("RTR 검증: 리프레시 토큰 갱신 시 DB의 토큰 값이 변경되어야 한다.")
    void refreshTokens_UpdatesDatabase() throws Exception {
        // 1. Given: 최초 토큰 생성 및 저장
        String oldRefreshToken = tokenProvider.generateToken(testUser, Duration.ofDays(1));
        refreshTokenRepository.save(new RefreshToken(testUser, oldRefreshToken));

        // iat(발급시간) 차이를 위해 아주 잠시 대기
        Thread.sleep(1000);

        // 2. When: 토큰 갱신 실행
        TokenService.TokenPair result = tokenService.refreshTokens(oldRefreshToken);

        // 3. Then: DB 검증
        RefreshToken savedEntity = refreshTokenRepository.findByUser(testUser).orElseThrow();

        assertThat(result.refreshToken()).isNotEqualTo(oldRefreshToken); // 새 토큰 확인
        assertThat(savedEntity.getRefreshToken()).isEqualTo(result.refreshToken()); // DB 동기화 확인
        // BaseTimeEntity 확인: 수정 시간이 생성 시간보다 나중이어야 함
        assertThat(savedEntity.getUpdatedAt()).isAfter(savedEntity.getCreatedAt());
    }
}
