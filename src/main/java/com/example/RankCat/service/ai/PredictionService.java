package com.example.RankCat.service.ai;

import com.example.RankCat.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final RestTemplate fastApiRestTemplate;
    @Value("${FASTAPI_URL}")
    private String fastApiUrl;
    // [추가] FastAPI 서버의 상태를 체크하는 테스트 메소드
    public HealthResponse checkAiServerHealth() {
        String healthEndpoint = fastApiUrl + "/health";

        // GET 요청을 보내고, 응답을 HealthResponse 객체로 받습니다.
        return fastApiRestTemplate.getForObject(healthEndpoint, HealthResponse.class);
    }
}
