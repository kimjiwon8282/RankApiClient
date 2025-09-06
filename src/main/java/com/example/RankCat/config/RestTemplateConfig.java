package com.example.RankCat.config;

import lombok.RequiredArgsConstructor;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final NaverSearchAdProperties adprops; //위의 빈 주입(라이선스,키,아이디 등)
    private final ShoppingInsightProperties insightProps;

    @Bean
    public RestTemplate naverRestTemplate() { //커스텀 RestTemplate 빈 등록
        CloseableHttpClient client = HttpClients.custom().build(); //엔진 인스턴스 생성 (커넥션 풀‧타임아웃 설정 가능)
        RestTemplate rt = new RestTemplate( //RestTemplate에 커스텀 HTTP 엔진 연결
                new HttpComponentsClientHttpRequestFactory(client));

        // 기본 Base URL 세팅-> 이후 서비스에서 경로만 넘기면 /keywordstool처럼 사용
        rt.setUriTemplateHandler(
                new DefaultUriBuilderFactory("https://api.naver.com"));
        // 공통 헤더 인터셉터 + 호출마다 공통 헤더를 삽입함
        rt.getInterceptors().add((req, body, ex) -> { // 매 요청을 보내기 직전 공통 로직 수행
            String timestamp = String.valueOf(System.currentTimeMillis()); //네이버 api요구 헤더용 타임스탬프
            String signature = com.example.RankCat.util.HmacSHA256Util.sign(
                    adprops.getSecretKey(), timestamp,
                    req.getMethod().name(),
                    req.getURI().getPath());

            req.getHeaders().add("X-Timestamp", timestamp); //필수 인증 헤더 주입
            req.getHeaders().add("X-API-KEY", adprops.getAccessLicense());
            req.getHeaders().add("X-CUSTOMER", adprops.getCustomerId());
            req.getHeaders().add("X-Signature", signature);
            return ex.execute(req, body); //수정된 요청을 체인에 전달 -> 그 후 실제 전송
        });
        return rt; //RestTemplate를 컨테이너에 반환
    }

    @Bean
    public RestTemplate shoppingInsightRestTemplate() {
        CloseableHttpClient client = HttpClients.custom().build(); //엔진 인스턴스 생성(커넥션풀,타임아웃)
        RestTemplate rt = new RestTemplate( //커스텀 HTTP엔진 연결
                new HttpComponentsClientHttpRequestFactory(client));
        rt.setUriTemplateHandler(
                new DefaultUriBuilderFactory("https://openapi.naver.com"));
        rt.getInterceptors().add((req, body, ex) -> {
            req.getHeaders().add("X-Naver-Client-Id",     insightProps.getId());
            req.getHeaders().add("X-Naver-Client-Secret", insightProps.getSecret());
            // Content-Type은 POST/PUT 등 바디 있는 요청에만 추가 (GET은 절대 추가하지 않음)
            if(!req.getMethod().name().equals("GET")) {
                req.getHeaders().add("Content-Type",          "application/json");
            }
            return ex.execute(req, body);
        });
        return rt;
    }

    @Bean
    public RestTemplate fastApiRestTemplate() {
        // 내부 API 통신용이므로 특별한 헤더 설정 없이 가장 기본적인 형태로 생성합니다.
        return new RestTemplate();
    }
}
