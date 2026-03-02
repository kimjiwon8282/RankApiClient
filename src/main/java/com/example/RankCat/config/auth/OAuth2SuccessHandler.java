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

    /** 최종 리다이렉트 경로 (예: /articles?token=...) */
    public static final String REDIRECT_PATH = "/home";

    // --- 생성자 주입 컴포넌트 ---
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;
    private final CookieUtil cookieUtil;

    /**
     * 로그인 성공 시 호출되는 메인 로직
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보 (OAuth2User 포함)
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        // 1) OAuth2User (소셜 유저 정보)를 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email;
        if (oAuth2User.getAttributes().containsKey("response")) {
            Map<String, Object> responseMap =
                    (Map<String, Object>) oAuth2User.getAttributes().get("response");
            email = (String) responseMap.get("email");
        } else {
            email = (String) oAuth2User.getAttributes().get("email");
        }

        // 2) DB에서 실제 User 엔티티 조회 (이메일 기준)
        User user = userService.findByEmail(email);

        // 3) Refresh Token 생성 → DB 저장 → 쿠키에 등록
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        // 4) Access Token: URL 노출 대신 '임시 쿠키'에 담아서 전달 (HttpOnly = false)            String
        // accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        int accessTokenMaxAge = 60;
        // 2. 주입받은 cookieUtil 인스턴스 메서드 호출
        cookieUtil.addCookie(
                response, ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenMaxAge, false);
        // 5) 임시 인증 데이터 (쿠키, 세션 등) 정리
        clearAuthenticationAttributes(request, response);

        // 6) 최종 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    /** 리프레시 토큰을 DB에 저장하거나 업데이트 */
    private void saveRefreshToken(Long userId, String newRefreshToken) {
        // 기존 토큰이 있으면 업데이트, 없으면 새로 생성
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByUserId(userId)
                        .map(entity -> entity.update(newRefreshToken))
                        .orElse(new RefreshToken(userId, newRefreshToken));
        // 저장 (insert or update)
        refreshTokenRepository.save(refreshToken);
    }

    /** 리프레시 토큰을 HTTP 쿠키에 추가 */
    private void addRefreshTokenToCookie(
            HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int maxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        // 3. 주입받은 cookieUtil 인스턴스 메서드 호출
        cookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        cookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAge);
    }

    /** 스프링 인증 관련 임시 속성 및 쿠키(인가 요청 정보) 정리 */
    private void clearAuthenticationAttributes(
            HttpServletRequest request, HttpServletResponse response) {
        // 부모 구현: 세션 기반 인증 속성 제거 (CSRF, state 등)
        super.clearAuthenticationAttributes(request);
        // 쿠키 기반 인가 요청 정보 삭제
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
