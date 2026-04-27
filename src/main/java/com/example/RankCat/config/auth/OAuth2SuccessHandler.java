package com.example.RankCat.config.auth;

import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.service.user.impl.UserService;
import com.example.RankCat.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /** 쿠키에 저장할 리프레시 토큰 이름 */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /** 프론트엔드 전달용 임시 액세스 토큰 쿠키 이름 */
    public static final String ACCESS_TOKEN_COOKIE_NAME = "temp_access_token";

    /** 리프레시 토큰 유효 기간 (3일) */
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(3);

    /** 액세스 토큰 유효 기간 (1일) */
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

    /** OAuth 성공 후 프론트엔드로 이동할 URL */
    @Value("${app.frontend.oauth-success-url:/home}")
    private String oauthSuccessUrl;

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final RefreshTokenHashService refreshTokenHashService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email;
        if (oAuth2User.getAttributes().containsKey("response")) {
            Map<String, Object> responseMap =
                    (Map<String, Object>) oAuth2User.getAttributes().get("response");
            email = (String) responseMap.get("email");
        } else {
            email = (String) oAuth2User.getAttributes().get("email");
        }

        User user = userService.findByEmail(email);

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user, refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        int accessTokenMaxAge = 60;

        cookieUtil.addCookie(
                response, ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenMaxAge, false);

        clearAuthenticationAttributes(request, response);

        getRedirectStrategy().sendRedirect(request, response, oauthSuccessUrl);
    }

    /** 리프레시 토큰을 DB에 저장하거나 업데이트 */
    private void saveRefreshToken(User user, String newRefreshToken) {
        String refreshTokenHash = refreshTokenHashService.hash(newRefreshToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByUser(user)
                        .map(entity -> entity.update(refreshTokenHash))
                        .orElse(new RefreshToken(user, refreshTokenHash));

        refreshTokenRepository.save(refreshToken);
    }

    /** 리프레시 토큰을 HTTP 쿠키에 추가 */
    private void addRefreshTokenToCookie(
            HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int maxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        cookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        cookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAge);
    }

    /** 스프링 인증 관련 임시 속성 및 쿠키 정리 */
    private void clearAuthenticationAttributes(
            HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
