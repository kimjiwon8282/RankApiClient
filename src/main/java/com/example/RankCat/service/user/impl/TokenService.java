package com.example.RankCat.service.user.impl;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    // 액세스 토큰은 발급 시점부터 2시간
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(2);

    @Transactional // RTR은 DB 업데이트가 동반되므로 필수
    public TokenPair refreshTokens(String refreshToken) {
        // 1. 기존 리프레시 토큰 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. DB에서 토큰 정보 및 유저 조회
        RefreshToken refreshTokenObj = refreshTokenService.findByRefreshToken(refreshToken);
        User user = userService.findById(refreshTokenObj.getUserId());

        // 3. 수명 승계 전략: 기존 토큰의 만료 시각을 추출
        Date oldExpiry = tokenProvider.getExpiration(refreshToken);

        // 4. 새로운 토큰 쌍 생성
        // Access Token: 신규 생성 (2시간)
        String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        // Refresh Token: 기존 만료 시각(oldExpiry)을 그대로 사용하여 재발급 (RTR)
        String newRefreshToken = tokenProvider.generateTokenWithExpiry(user, oldExpiry);

        // 5. DB 업데이트 (Rotation)
        refreshTokenObj.update(newRefreshToken);
        refreshTokenRepository.save(refreshTokenObj);

        return new TokenPair(newAccessToken, newRefreshToken, oldExpiry);
    }

    public record TokenPair(String accessToken, String refreshToken, Date expiry) {}
}
