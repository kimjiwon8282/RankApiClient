package com.example.RankCat.controller.user;

import com.example.RankCat.config.auth.AuthService;
import com.example.RankCat.dto.user.CreateAccessTokenResponse;
import com.example.RankCat.dto.user.LoginRequest;
import com.example.RankCat.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2. 인증 및 로그인", description = "사용자 로그인 및 토큰 발급 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppLoginController {

    private final AuthService authService; // 핵심 로직 위임
    private final CookieUtil cookieUtil;

    @Operation(summary = "앱 전용 로그인", description = "이메일/비밀번호로 로그인하고 AccessToken을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "로그인 실패 (비밀번호 불일치 또는 미가입)",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                name = "U001",
                                                value =
                                                        "{\"status\":401,\"code\":\"U001\",\"message\":\"이메일 또는 비밀번호가 일치하지 않습니다.\"}")))
    })
    @PostMapping("/login")
    public CreateAccessTokenResponse login(
            @RequestBody LoginRequest req, HttpServletResponse response) {
        // 1. 서비스에 로직 위임
        AuthService.LoginResponse loginResponse = authService.login(req);

        // 2. 쿠키 설정 (컨트롤러의 역할)
        cookieUtil.addCookie(
                response,
                "refresh_token",
                loginResponse.refreshToken(),
                (int) Duration.ofDays(3).getSeconds());

        // 3. 응답 반환
        return new CreateAccessTokenResponse(loginResponse.accessToken());
    }
}
