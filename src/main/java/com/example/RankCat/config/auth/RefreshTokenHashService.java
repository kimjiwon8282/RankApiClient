package com.example.RankCat.config.auth;

import com.example.RankCat.config.jwt.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenHashService {
    private static final String ALG = "HmacSHA256";
    private final JwtProperties jwtProperties;

    public String hash(String refreshToken) {
        try {
            Mac mac = Mac.getInstance(ALG);
            mac.init(
                    new SecretKeySpec(
                            jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8), ALG));
            byte[] digest = mac.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }
}
