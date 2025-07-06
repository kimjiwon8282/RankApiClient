package com.example.RankCat.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class CookieUtil {
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return;
        }
        for(Cookie cookie : cookies) {
            if(name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }


    /**
     * 자바 객체를 Base64 URL-safe 문자열로 직렬화합니다.
     * 주로 쿠키에 복잡한 값을 저장할 때 사용합니다.
     *
     * @param object    직렬화할 객체
     * @return          Base64로 인코딩된 문자열
     */
    public static String serialize(Object object) {
        // 스프링의 SerializationUtils 로 바이트 배열로 직렬화 → URL-safe Base64 인코딩
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * 쿠키에 저장된 Base64 문자열을 다시 객체로 역직렬화합니다.
     *
     * @param cookie    값을 읽을 쿠키
     * @param cls       역직렬화할 객체의 타입 클래스
     * @param <T>       반환 타입
     * @return          역직렬화된 객체
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        // 쿠키의 문자열을 디코딩 → SerializationUtils 로 객체 복원 → 원하는 타입으로 캐스팅
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }
}
