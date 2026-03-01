package com.example.RankCat.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.Role;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("local") // 로컬 프로필 활성화 (H2, Lax 쿠키 등 사용)
@SpringBootTest(
        properties = {
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
            // 아래 코드를 추가하여 CORS 설정을 채워줍니다.
            "cors.allowed-origins=http://localhost:3000,http://localhost:5173"
        })
@AutoConfigureMockMvc
class TokenApiControllerTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected TokenProvider tokenProvider;

    @Autowired protected UserRepository userRepository;

    @Autowired protected RefreshTokenRepository refreshTokenRepository;

    User testUser;

    @BeforeEach
    void setFixture() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 유저 생성
        testUser =
                userRepository.save(
                        User.builder()
                                .email("test@example.com")
                                .password("password")
                                .role(Role.USER)
                                .build());
    }

    @DisplayName("createNewAccessToken: RTR 로직에 따라 새로운 액세스 토큰과 갱신된 리프레시 토큰을 반환한다.")
    @Test
    public void createNewAccessToken_RTR_Success() throws Exception {
        // given
        // 1. 초기 리프레시 토큰 생성 및 DB 저장
        String initialRefreshToken = tokenProvider.generateToken(testUser, Duration.ofDays(3));
        refreshTokenRepository.save(new RefreshToken(testUser.getId(), initialRefreshToken));

        // when
        // 2. /api/token 에 쿠키를 실어 요청 보냄
        final String url = "/api/token";
        ResultActions resultActions =
                mockMvc.perform(
                        post(url)
                                .cookie(new Cookie("refresh_token", initialRefreshToken))
                                .contentType(MediaType.APPLICATION_JSON));

        // then
        // 3. 응답 검증: 201 Created 확인
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty()); // 새 액세스 토큰 확인

        // 4. RTR 검증: 응답 헤더의 쿠키가 초기 토큰과 달라야 함
        String setCookieHeader = resultActions.andReturn().getResponse().getHeader("Set-Cookie");
        assertThat(setCookieHeader).contains("refresh_token=");
        assertThat(setCookieHeader).doesNotContain(initialRefreshToken); // 토큰 값이 바뀌었는지 확인

        // 5. DB 검증: DB에 저장된 토큰도 초기 토큰과 달라야 함
        RefreshToken savedToken = refreshTokenRepository.findByUserId(testUser.getId()).get();
        assertThat(savedToken.getRefreshToken()).isNotEqualTo(initialRefreshToken);
    }

    @DisplayName("createNewAccessToken: 리프레시 토큰이 만료되었으면 재발급에 실패한다.")
    @Test
    public void createNewAccessToken_ExpiredRefreshToken_Fail() throws Exception {
        // given: 이미 만료된 토큰 생성 (Duration을 마이너스로 설정)
        String expiredRefreshToken = tokenProvider.generateToken(testUser, Duration.ofDays(-1));
        refreshTokenRepository.save(new RefreshToken(testUser.getId(), expiredRefreshToken));

        // when: 만료된 토큰으로 요청
        final String url = "/api/token";
        ResultActions resultActions =
                mockMvc.perform(post(url).cookie(new Cookie("refresh_token", expiredRefreshToken)));

        // then: 실패 응답(401 또는 BusinessException 설정값) 확인
        resultActions.andExpect(status().isUnauthorized()); // ErrorCode 설정에 따라 401 또는 400
    }
}
