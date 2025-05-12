package com.example.RankCat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration //설정 클래스로 등록 ->클래스의 인스턴스 자체가 빈으로 등록
@ConfigurationProperties(prefix = "naver.search.ad") //yml 경로 값을 필드 자동 매핑
public class NaverSearchAdProperties {
    private String accessLicense;
    private String secretKey;
    private String customerId;
}
