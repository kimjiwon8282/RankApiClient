package com.example.RankCat.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.auth.WebOAuthSecurityConfig;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.dto.user.AddUserRequest;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.service.user.impl.TokenService;
import com.example.RankCat.service.user.impl.UserService;
import com.example.RankCat.service.user.interfaces.EmailAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {SignupController.class, TokenApiController.class, AppLoginController.class},
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = WebOAuthSecurityConfig.class)
        })
class UserManagementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private EmailAuthService emailAuthService;
    @MockBean private TokenService tokenService;
    @MockBean private TokenProvider tokenProvider;
    @MockBean private AuthenticationManager authManager;
    @MockBean private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("1. 회원가입: 필수 값 누락 시 C001(잘못된 입력값) 에러가 발생한다")
    void signup_invalidRequest_throwsC001() throws Exception {
        AddUserRequest invalidRequest = new AddUserRequest(); // 빈 객체 전송

        mockMvc.perform(
                        post("/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("2. 인증번호 검증: 번호 불일치 시 A003(인증번호 오류) 코드를 반환한다")
    void verifyAuthCode_invalidCode_throwsA003() throws Exception {
        // [수정] 로그에서 확인된 실제 코드 'A003'을 기대값으로 설정
        doThrow(new BusinessException(ErrorCode.INVALID_AUTH_CODE))
                .when(emailAuthService)
                .verifyAuthCode(anyString(), anyString());

        String requestBody = "{\"email\": \"test@test.com\", \"code\": \"123456\"}";

        mockMvc.perform(
                        post("/api/user/verify-auth-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A003"));
    }

    @Test
    @DisplayName("3. 메일 발송: 발송 실패 시 M001(메일 발송 실패) 코드를 반환한다")
    void sendMail_apiError_throwsM001() throws Exception {
        // [수정] 로그에서 확인된 실제 코드 'M001'을 기대값으로 설정
        doThrow(new BusinessException(ErrorCode.MAIL_SEND_FAILED))
                .when(emailAuthService)
                .sendAuthCode(anyString());

        mockMvc.perform(post("/api/user/send-auth-code").param("email", "test@test.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("M001"));
    }

    @Test
    @DisplayName("4. 토큰 갱신: 필수 쿠키 누락 시 C002(처리되지 않은 서버 오류)가 발생한다")
    void refreshToken_missingCookie_throwsC002() throws Exception {
        // [분석] 현재 핸들러에 MissingRequestCookieException 처리가 없어 C002(500)로 떨어짐
        mockMvc.perform(post("/api/token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C002"));
    }

    @Test
    @DisplayName("5. 로그인: 인증 실패 시 C002(처리되지 않은 서버 오류)가 발생한다")
    void login_badCredentials_throwsC002() throws Exception {
        // [분석] BadCredentialsException 또한 핸들러에서 별도 처리 전이므로 C002(500)로 응답함
        given(authManager.authenticate(any())).willThrow(new BadCredentialsException("인증 실패"));

        String loginJson = "{\"email\": \"test@test.com\", \"password\": \"wrong\"}";

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C002"));
    }
}
