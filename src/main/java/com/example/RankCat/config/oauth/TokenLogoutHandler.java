package com.example.RankCat.config.oauth;

import com.example.RankCat.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenLogoutHandler implements LogoutHandler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 1. 쿠키에서 리프레시 토큰 찾기
        // (OAuth2SuccessHandler 등에서 사용한 쿠키 이름과 동일해야 함)
        Cookie cookie = WebUtils.getCookie(request, "refresh_token");

        if (cookie != null) {
            String refreshToken = cookie.getValue();

            // 2. DB에서 해당 리프레시 토큰 삭제
            refreshTokenRepository.findByRefreshToken(refreshToken)
                    .ifPresent(token -> {
                        refreshTokenRepository.delete(token);
                        log.info("로그아웃: DB에서 리프레시 토큰 삭제 완료. ID={}", token.getId());
                    });
        }
    }
}