package com.example.RankCat.config.oauth;


import com.example.RankCat.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;


/**
 * OAuth2 인가 요청 정보를 HTTP 쿠키에 저장하고,
 * 다시 쿠키에서 꺼내오는 저장소 구현체
 */
public class OAuth2AuthorizationRequestBasedOnCookieRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    /** 쿠키 이름 (인가 요청 정보 저장) */
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    /** 쿠키 수명 (초 단위) */
    private static final int COOKIE_EXPIRE_SECONDS = 18000; // 5시간

    /**
     * Authorization 요청을 제거(remove)하면서,
     * 실제로는 loadAuthorizationRequest를 호출해 쿠키에서 꺼냄
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 스프링 내부에서 인가 요청 사용 후 cleanup을 위해 호출됨
        return this.loadAuthorizationRequest(request);
    }

    /**
     * 현재 요청에 대응하는 OAuth2AuthorizationRequest를
     * 쿠키에서 꺼내 역직렬화하여 반환
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // 요청 내 쿠키 중 지정된 이름의 쿠키를 찾음
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        // 쿠키가 null이거나 만료됐으면 deserialize 내부에서 null 반환
        return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
    }

    /**
     * OAuth2AuthorizationRequest 객체를 쿠키에 저장
     * (인가 요청 단계에서 AuthorizationRequestRedirectFilter가 호출)
     */
    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        // authorizationRequest가 null이면 삭제 로직 수행
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        // 인가 요청 객체를 바이트 배열로 직렬화 후 Base64로 인코딩하여 쿠키 저장
        CookieUtil.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtil.serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS
        );
    }

    /**
     * 명시적으로 인가 요청 쿠키를 삭제
     */
    public void removeAuthorizationRequestCookies(
            HttpServletRequest request,
            HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
