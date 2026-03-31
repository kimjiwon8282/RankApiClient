package com.example.RankCat.config.auth;

import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.dto.user.LoginRequest;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHashService refreshTokenHashService;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = (User) authentication.getPrincipal();

        String accessToken = tokenProvider.generateToken(user, Duration.ofHours(1));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(3));

        updateRefreshToken(user, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    private void updateRefreshToken(User user, String refreshToken) {
        String hashedRefreshToken = refreshTokenHashService.hash(refreshToken);

        RefreshToken tokenEntity =
                refreshTokenRepository
                        .findByUser(user)
                        .map(rt -> rt.update(hashedRefreshToken))
                        .orElse(new RefreshToken(user, hashedRefreshToken));

        refreshTokenRepository.save(tokenEntity);
    }

    public record LoginResponse(String accessToken, String refreshToken) {}
}
