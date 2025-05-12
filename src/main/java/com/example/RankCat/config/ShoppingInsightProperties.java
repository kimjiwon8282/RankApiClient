package com.example.RankCat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter@Setter
@Configuration
@ConfigurationProperties(prefix = "naver.shoppinginsight.client")
public class ShoppingInsightProperties {
    private String id;
    private String secret;
}
