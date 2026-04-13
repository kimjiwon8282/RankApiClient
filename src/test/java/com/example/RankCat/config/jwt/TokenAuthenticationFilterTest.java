package com.example.RankCat.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock private TokenProvider tokenProvider;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 인증 정보를 저장한다")
    void doFilterInternal_validBearerToken_setsAuthentication()
            throws ServletException, IOException {
        // given
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "tester@example.com",
                        "valid-token",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        given(tokenProvider.validateToken("valid-token")).willReturn(true);
        given(tokenProvider.getAuthentication("valid-token")).willReturn(authentication);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isEqualTo(authentication);
        verify(tokenProvider).validateToken("valid-token");
        verify(tokenProvider).getAuthentication("valid-token");
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보를 저장하지 않고 다음 필터로 진행한다")
    void doFilterInternal_withoutAuthorizationHeader_skipsAuthentication()
            throws ServletException, IOException {
        // given
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider).validateToken(null);
        verify(tokenProvider, never()).getAuthentication(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 헤더면 인증 정보를 저장하지 않는다")
    void doFilterInternal_nonBearerHeader_skipsAuthentication()
            throws ServletException, IOException {
        // given
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abcdefg");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider).validateToken(null);
        verify(tokenProvider, never()).getAuthentication(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Bearer 토큰이 있어도 유효하지 않으면 인증 정보를 저장하지 않는다")
    void doFilterInternal_invalidBearerToken_skipsAuthentication()
            throws ServletException, IOException {
        // given
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(tokenProvider.validateToken("invalid-token")).willReturn(false);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider).validateToken("invalid-token");
        verify(tokenProvider, never()).getAuthentication(org.mockito.ArgumentMatchers.any());
    }
}
