package com.example.RankCat.config.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.Role;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock private TokenProvider tokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RefreshTokenHashService refreshTokenHashService;

    @InjectMocks private TokenService tokenService;

    @Test
    @DisplayName("유효하지 않은 refresh token이면 INVALID_TOKEN 예외를 던진다")
    void refreshTokens_invalidToken_throwsInvalidToken() {
        // given
        String refreshToken = "invalid-refresh-token";
        given(tokenProvider.validateToken(refreshToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> tokenService.refreshTokens(refreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);

        verify(refreshTokenService, never()).findWithUserByRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("유효한 refresh token이면 access/refresh 토큰을 재발급하고 해시를 갱신한다")
    void refreshTokens_success_rotatesTokenAndUpdatesHash() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";
        Date oldExpiry = new Date(System.currentTimeMillis() + 60_000L);

        User user =
                User.builder()
                        .email("tester@example.com")
                        .password("encoded-password")
                        .nickname("tester")
                        .role(Role.USER)
                        .build();
        RefreshToken refreshTokenEntity = new RefreshToken(user, "old-refresh-token-hash");

        given(tokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(refreshTokenService.findWithUserByRefreshToken(oldRefreshToken))
                .willReturn(refreshTokenEntity);
        given(tokenProvider.getExpiration(oldRefreshToken)).willReturn(oldExpiry);
        given(tokenProvider.generateToken(eq(user), eq(TokenService.ACCESS_TOKEN_DURATION)))
                .willReturn(newAccessToken);
        given(tokenProvider.generateTokenWithExpiry(user, oldExpiry)).willReturn(newRefreshToken);
        given(refreshTokenHashService.hash(newRefreshToken)).willReturn(newRefreshTokenHash);

        // when
        TokenService.TokenPair result = tokenService.refreshTokens(oldRefreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
        assertThat(result.expiry()).isEqualTo(oldExpiry);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken).isSameAs(refreshTokenEntity);
        assertThat(savedToken.getRefreshToken()).isEqualTo(newRefreshTokenHash);

        verify(tokenProvider).generateToken(user, TokenService.ACCESS_TOKEN_DURATION);
        verify(tokenProvider).generateTokenWithExpiry(user, oldExpiry);
    }
}
