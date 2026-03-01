package com.example.RankCat.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class CookieUtil {

    @Value("${cookie.same-site}")
    private String sameSite;

    @Value("${cookie.secure}")
    private boolean secure;

    // 1. 기본 쿠키 추가 (HttpOnly = true)
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        addCookie(response, name, value, maxAge, true);
    }

    // 2. HttpOnly 여부를 제어할 수 있는 쿠키 추가
    public void addCookie(
            HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        ResponseCookie cookie =
                ResponseCookie.from(name, value)
                        .path("/")
                        .maxAge(maxAge)
                        .httpOnly(httpOnly)
                        .secure(secure) // yml 설정에 따라 local은 false, prod는 true
                        .sameSite(sameSite) // yml 설정에 따라 local은 Lax, prod는 None
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 3. 쿠키 삭제 (ResponseCookie 방식으로 통일)
    public void deleteCookie(
            HttpServletRequest request, HttpServletResponse response, String name) {
        ResponseCookie cookie =
                ResponseCookie.from(name, "")
                        .path("/")
                        .maxAge(0)
                        .secure(secure)
                        .sameSite(sameSite)
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 자바 객체를 Base64 URL-safe 문자열로 직렬화합니다. 주로 쿠키에 복잡한 값을 저장할 때 사용합니다.
     *
     * @param object 직렬화할 객체
     * @return Base64로 인코딩된 문자열
     */
    public static String serialize(Object object) {
        // 스프링의 SerializationUtils 로 바이트 배열로 직렬화 → URL-safe Base64 인코딩
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * 쿠키에 저장된 Base64 문자열을 다시 객체로 역직렬화합니다.
     *
     * @param cookie 값을 읽을 쿠키
     * @param cls 역직렬화할 객체의 타입 클래스
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isEmpty()) {
            return null;
        }

        // 2. 값이 있을 때만 디코딩 및 역직렬화 수행
        byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
        return cls.cast(SerializationUtils.deserialize(decodedBytes));
    }
}
