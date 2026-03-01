package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.CreateAccessTokenResponse;
import com.example.RankCat.service.user.impl.TokenService;
import com.example.RankCat.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenApiController {

    private final TokenService tokenService;
    private final CookieUtil cookieUtil; // 주입받은 Bean 사용

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
