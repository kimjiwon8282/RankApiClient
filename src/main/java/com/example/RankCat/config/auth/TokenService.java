package com.example.RankCat.config.auth;

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
    private final RefreshTokenHashService refreshTokenHashService;

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(2);

    @Transactional
    public TokenPair refreshTokens(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken refreshTokenObj = refreshTokenService.findWithUserByRefreshToken(refreshToken);
        User user = refreshTokenObj.getUser();

        Date oldExpiry = tokenProvider.getExpiration(refreshToken);

        String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String newRefreshToken = tokenProvider.generateTokenWithExpiry(user, oldExpiry);

        refreshTokenObj.update(refreshTokenHashService.hash(newRefreshToken));
        refreshTokenRepository.save(refreshTokenObj);

        return new TokenPair(newAccessToken, newRefreshToken, oldExpiry);
    }

    public record TokenPair(String accessToken, String refreshToken, Date expiry) {}
}
