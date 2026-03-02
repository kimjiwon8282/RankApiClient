package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.CreateAccessTokenResponse;
import com.example.RankCat.service.user.impl.TokenService;
import com.example.RankCat.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. 토큰 관리", description = "Access Token 재발급 및 관리 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenApiController {

    private final TokenService tokenService;
    private final CookieUtil cookieUtil; // 주입받은 Bean 사용

    @Operation(
            summary = "Access Token 재발급 (RTR 방식)",
            description =
                    "### 👨‍💻 프론트엔드 연동 가이드\n\n"
                            + "**[1. 평상시 API 통신]**\n"
                            + "모든 인증이 필요한 API 요청 시, HTTP 헤더에 Access Token을 담아주세요.\n"
                            + "`Authorization: Bearer {access_token}`\n\n"
                            + "**[2. 토큰 만료 시 재발급 흐름 (Axios Interceptor 권장)]**\n"
                            + "1. API 호출 중 서버로부터 **401 Unauthorized** 에러를 응답받으면 토큰이 만료된 것입니다.\n"
                            + "2. 에러를 캐치한 즉시 현재 API(`POST /api/token`)를 호출하여 재발급을 요청합니다.\n"
                            + "   - ⚠️ **매우 중요**: 이 요청을 보낼 때는 반드시 `withCredentials: true` 옵션을 설정해야 브라우저에 숨겨진 `refresh_token` 쿠키가 서버로 안전하게 전달됩니다.\n"
                            + "3. 서버 응답(201 Created)에서 새로운 `access_token`을 꺼내어 로컬 스토리지(또는 상태 관리 도구)에 업데이트합니다.\n"
                            + "4. 401 에러로 실패했던 원래의 API 요청을 새로운 토큰으로 다시 재시도합니다.\n\n"
                            + "**[3. 백그라운드 보안 로직]**\n"
                            + "- 이 API가 성공하면 서버는 보안을 위해 `refresh_token` 쿠키도 새로운 값으로 교체(RTR)하여 응답 헤더에 담아 보냅니다. (프론트엔드에서는 브라우저가 알아서 쿠키를 갱신하므로 별도 처리가 필요 없습니다.)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "토큰 재발급 성공"),
        @ApiResponse(
                responseCode = "401",
                description =
                        "리프레시 토큰이 없거나 만료됨 (이 경우 프론트엔드는 사용자를 강제 로그아웃 처리하고 로그인 페이지로 이동시켜야 합니다.)")
    })
    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {

        // 1) RTR 실행 (수명 승계 방식)
        TokenService.TokenPair tokenPair = tokenService.refreshTokens(refreshToken);

        // 2) 쿠키 갱신: 남은 수명만큼 다시 쿠키를 구움
        long remainingSeconds = (tokenPair.expiry().getTime() - System.currentTimeMillis()) / 1000;
        cookieUtil.addCookie(
                response, "refresh_token", tokenPair.refreshToken(), (int) remainingSeconds);

        log.info("RTR complete with Absolute Expiration: tokens rotated");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(tokenPair.accessToken()));
    }
}
