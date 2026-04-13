package com.example.RankCat.config.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.dto.user.LoginRequest;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.Role;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenProvider tokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RefreshTokenHashService refreshTokenHashService;
    @Mock private Authentication authentication;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 access/refresh 토큰을 발급하고 해시된 refresh token을 저장한다")
    void login_success_savesHashedRefreshToken() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("tester@example.com");
        request.setPassword("password123!");

        User user =
                User.builder()
                        .email("tester@example.com")
                        .password("encoded-password")
                        .nickname("tester")
                        .role(Role.USER)
                        .build();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String hashedRefreshToken = "hashed-refresh-token";

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(user);
        given(tokenProvider.generateToken(eq(user), eq(Duration.ofHours(1))))
                .willReturn(accessToken);
        given(tokenProvider.generateToken(eq(user), eq(Duration.ofDays(3))))
                .willReturn(refreshToken);
        given(refreshTokenHashService.hash(refreshToken)).willReturn(hashedRefreshToken);
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

        // when
        AuthService.LoginResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authTokenCaptor.capture());
        assertThat(authTokenCaptor.getValue().getPrincipal()).isEqualTo("tester@example.com");
        assertThat(authTokenCaptor.getValue().getCredentials()).isEqualTo("password123!");

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getRefreshToken()).isEqualTo(hashedRefreshToken);
    }

    @Test
    @DisplayName("기존 refresh token 레코드가 있으면 새로 만들지 않고 해시값만 갱신한다")
    void login_existingRefreshToken_updatesExistingEntity() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("tester@example.com");
        request.setPassword("password123!");

        User user =
                User.builder()
                        .email("tester@example.com")
                        .password("encoded-password")
                        .nickname("tester")
                        .role(Role.USER)
                        .build();

        RefreshToken existingToken = new RefreshToken(user, "old-hash");

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(user);
        given(tokenProvider.generateToken(eq(user), eq(Duration.ofHours(1))))
                .willReturn("new-access-token");
        given(tokenProvider.generateToken(eq(user), eq(Duration.ofDays(3))))
                .willReturn("new-refresh-token");
        given(refreshTokenHashService.hash("new-refresh-token")).willReturn("new-hash");
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(existingToken));

        // when
        authService.login(request);

        // then
        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken).isSameAs(existingToken);
        assertThat(existingToken.getRefreshToken()).isEqualTo("new-hash");
    }
}
