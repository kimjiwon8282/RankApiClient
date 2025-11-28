package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.CreateAccessTokenRequest;
import com.example.RankCat.dto.user.CreateAccessTokenResponse;
import com.example.RankCat.service.user.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor  // final 필드(TokenService) 자동 주입
@Slf4j
public class TokenApiController {

    private final TokenService tokenService;  // 리프레시 토큰 검증 및 액세스 토큰 생성 로직

    /**
     * 클라이언트가 보유한 HttpOnly 쿠키(refresh_token)를 이용해
     * 새로운 액세스 토큰을 발급합니다.
     */
    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @CookieValue("refresh_token") String refreshToken
    ) {
        // 1) 쿠키에서 꺼낸 토큰으로 검증 및 재발급 진행
        String newAccessToken = tokenService.createNewAccessToken(refreshToken);
        log.info("New access token created via HttpOnly Cookie");
        // 2) HTTP 201 상태 코드와 함께 새 토큰 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));

    }
}
