package com.example.RankCat.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="jwt")
public class JwtProperties {
    private String issuer;
    private String secretKey;// YAML 의 secret-key 가 바인딩
}

