package com.example.RankCat.config.jwt;

import com.example.RankCat.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class TokenProvider {
    // application.yml 에 설정된 issuer, secretKey 값을 주입받음
    private final JwtProperties jwtProperties;

    /**
     * 주어진 사용자 정보와 유효기간(duration)을 바탕으로 JWT 토큰을 생성합니다.
     *
     * @param user     토큰에 담을 사용자 엔티티 (이메일, ID)
     * @param duration 토큰 만료까지의 기간 (예: Duration.ofHours(1))
     * @return 생성된 JWT 문자열
     */
    public String generateToken(User user, Duration duration) {
        log.info("JWT issuer: {}", jwtProperties.getIssuer());
        log.info("JWT secretKey length: {}",
                jwtProperties.getSecretKey() == null
                        ? "null"
                        : jwtProperties.getSecretKey().length());
        Date now    = new Date();                                 // 현재 시각
        Date expiry = new Date(now.getTime() + duration.toMillis()); // 만료 시각 = 현재 + duration(밀리초)
        return makeToken(expiry, user);
    }

    /**
     * JWT 헤더·클레임·서명을 구성하여 실제 토큰 문자열을 반환합니다.
     *
     * @param expiry 토큰 만료 시각
     * @param user   토큰에 포함할 사용자 정보
     * @return 컴팩트된 JWT 토큰 문자열
     */
    private String makeToken(Date expiry, User user) {
        Date now = new Date();  // 토큰 발급 시각

        return Jwts.builder()
                // 헤더 타입을 JWT로 설정
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                // iss(issuer): 토큰 발급자 식별자
                .setIssuer(jwtProperties.getIssuer())
                // iat(issued at): 토큰 발급 시각
                .setIssuedAt(now)
                // exp(expiration): 토큰 만료 시각
                .setExpiration(expiry)
                // sub(subject): 토큰 주제, 여기서는 사용자 식별(email)
                .setSubject(user.getEmail())
                // 추가 클레임으로 사용자 ID 저장
                .claim("id", user.getId())
                // HS256 알고리즘, secretKey로 서명
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    /**
     * 전달된 JWT 토큰이 올바르게 서명되었고, 만료되지 않았는지 검증합니다.
     *
     * @param token 검증할 JWT 문자열
     * @return 검증 성공 시 true, 실패 시 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey()) // 비밀키로 서명 검증
                    .parseClaimsJws(token);                      // 파싱 시도 (예외 발생 시 invalid)
            return true;
        } catch (Exception e) {
            // 서명 불일치, 만료, 형식 오류 등 모든 예외를 false로 처리
            return false;
        }
    }

    /**
     * 유효한 JWT 토큰에서 Spring Security용 Authentication 객체를 생성합니다.
     * 이 객체를 시큐리티 컨텍스트에 저장하면, 이후 요청에서 인증된 사용자로 인식됩니다.
     *
     * @param token 인증정보를 추출할 JWT 토큰
     * @return UsernamePasswordAuthenticationToken (principal, credentials, authorities)
     */
    public Authentication getAuthentication(String token) {
        // 1) 클레임(페이로드) 조회
        Claims claims = getClaims(token);

        // 2) 권한(ROLE_USER) 설정 — 여기선 모든 토큰에 단일 ROLE_USER 부여
        Set<SimpleGrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        // 3) Spring Security User 객체 생성
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        claims.getSubject(),        // 사용자명 (email)
                        "",                         // 패스워드는 이미 토큰 검증으로 대체하므로 빈 문자열
                        authorities                 // 권한 목록
                );

        // 4) AuthenticationToken 생성: principal, credentials, authorities
        //    credentials 자리에 원래 토큰을 넣어 세션에서 토큰 재사용 가능
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWT 토큰에서 사용자 ID(id 클레임)만 추출합니다.
     *
     * @param token JWT 문자열
     * @return cliam "id" 에 담긴 Long 타입의 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }
    /**
     * JWT 토큰의 클레임(페이로드) 부분을 파싱하여 Claims 객체로 반환합니다.
     *
     * @param token 파싱할 JWT 문자열
     * @return Claims (iss, sub, exp, custom claims 등)
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey()) // 비밀키로 서명 검증 후 페이로드 반환
                .parseClaimsJws(token)
                .getBody();
    }
}