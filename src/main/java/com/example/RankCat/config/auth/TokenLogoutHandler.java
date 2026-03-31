package com.example.RankCat.config.auth;

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
    private final RefreshTokenHashService refreshTokenHashService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        Cookie cookie = WebUtils.getCookie(request, "refresh_token");

        if (cookie != null) {
            String refreshToken = cookie.getValue();
            String hashedRefreshToken = refreshTokenHashService.hash(refreshToken);

            refreshTokenRepository
                    .findByRefreshToken(hashedRefreshToken)
                    .ifPresent(
                            token -> {
                                refreshTokenRepository.delete(token);
                                log.info("로그아웃: DB에서 리프레시 토큰 삭제 완료. ID={}", token.getId());
                            });
        }
    }
}
