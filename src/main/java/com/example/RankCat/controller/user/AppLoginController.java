package com.example.RankCat.controller.user;

import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.dto.CreateAccessTokenResponse;
import com.example.RankCat.dto.LoginRequest;
import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.RefreshTokenRepository;
import com.example.RankCat.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppLoginController {
    private final AuthenticationManager authManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public CreateAccessTokenResponse login(
            @RequestBody LoginRequest req,
            HttpServletResponse response
    ) {
        // 1) 사용자 인증
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = (User) authentication.getPrincipal();

        // 2) 토큰 생성 (액세스 1시간, 리프레시 3일)
        String accessToken  = tokenProvider.generateToken(user, Duration.ofHours(1));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(3));

        // 3) 리프레시 토큰 DB 저장 또는 업데이트
        RefreshToken tokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .map(rt -> rt.update(refreshToken))
                .orElse(new RefreshToken(user.getId(), refreshToken));
        refreshTokenRepository.save(tokenEntity);

        // 4) HttpOnly 쿠키에 리프레시 토큰 설정
        CookieUtil.addCookie(
                response,
                "refresh_token",
                refreshToken,
                (int) Duration.ofDays(3).getSeconds()
        );

        // 5) 액세스 토큰은 JSON 바디로 반환
        return new CreateAccessTokenResponse(accessToken);
    }
}
