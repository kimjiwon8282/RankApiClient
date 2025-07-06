package com.example.RankCat.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization"; //Http 요청 헤더에 담기는 인증 토큰 키
    private final static String TOKEN_PREFIX = "Bearer "; //헤더 값 앞에 붙는 접두사(Bearer)


    //모든 HTTP요청마다 한번씩 실행되는 메서드
    //인증이 필요한 엔드포인트에 접근 할때, 헤더의 JWT토큰을 꺼내
    //유효성을 검사하고 인증 정보를 시큐리티 컨텍스트에 저장함
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,     // 클라이언트 요청 객체
            HttpServletResponse response,    // 서버 응답 객체
            FilterChain filterChain // 다음 필터 체인
    ) throws ServletException, IOException {

        // 1) 헤더에서 Authorization 값을 읽어온다.
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        // 2) "Bearer " 접두사를 제거해 실제 토큰 문자열만 추출
        String token = getAccessToken(authorizationHeader);

        // 3) 토큰이 있고 유효하다면
        if (tokenProvider.validateToken(token)) {
            // 4) 토큰에서 Authentication 객체(Principal, Authorities 등) 생성
            Authentication authentication = tokenProvider.getAuthentication(token);

            // 5) Spring SecurityContext 에 인증 정보를 저장
            //    → 이후 컨트롤러, 서비스 호출 시 SecurityContextHolder 에서 꺼내 쓸 수 있다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6) 필터 체인 이어가기 (다음 필터 또는 최종 서블릿 실행)
        filterChain.doFilter(request, response);
    }

    /**
     * 헤더 값에서 "Bearer " 접두사를 제거하고
     * 순수한 JWT 토큰 문자열만 반환합니다.
     *
     * @param authorizationHeader 실제 HTTP Authorization 헤더 값
     * @return "Bearer " 뒤의 토큰 또는 헤더가 없거나 형식이 잘못되면 null
     */
    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null
                && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            // 접두사 길이만큼 잘라내기
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}