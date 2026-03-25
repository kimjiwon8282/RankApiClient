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

    @Transactional
    public LoginResponse login(LoginRequest req) {
        // 1. 사용자 인증 (실패 시 시큐리티 예외 발생)
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = (User) authentication.getPrincipal();

        // 2. 토큰 생성
        String accessToken = tokenProvider.generateToken(user, Duration.ofHours(1));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(3));

        // 3. 리프레시 토큰 저장 및 업데이트
        updateRefreshToken(user, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    private void updateRefreshToken(User user, String refreshToken) {
        RefreshToken tokenEntity =
                refreshTokenRepository
                        .findByUser(user)
                        .map(rt -> rt.update(refreshToken))
                        .orElse(new RefreshToken(user, refreshToken));

        refreshTokenRepository.save(tokenEntity);
    }

    // 컨트롤러에 데이터를 전달하기 위한 내부 DTO
    public record LoginResponse(String accessToken, String refreshToken) {}
}
