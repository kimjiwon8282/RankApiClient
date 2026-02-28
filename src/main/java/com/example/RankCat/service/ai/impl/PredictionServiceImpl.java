package com.example.RankCat.service.ai.impl;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.ai.AiPredictRequest;
import com.example.RankCat.dto.ai.AiPredictResponse;
import com.example.RankCat.dto.ai.HealthResponse;
import com.example.RankCat.service.ai.interfaces.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {

    private final RestTemplate fastApiRestTemplate;

    @Value("${FASTAPI_URL}")
    private String fastApiUrl;

    @Override
    public HealthResponse checkAiServerHealth() {
        String healthEndpoint = fastApiUrl + "/health";
        try {
            // ✅ FastAPI 호출 보호
            return fastApiRestTemplate.getForObject(healthEndpoint, HealthResponse.class);
        } catch (Exception e) {
            log.error("FastAPI 서버 Health Check 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public AiPredictResponse predict(AiPredictRequest request) {
        String url = fastApiUrl + "/predict";
        try {
            // ✅ FastAPI 호출 보호
            return fastApiRestTemplate.postForObject(url, request, AiPredictResponse.class);
        } catch (Exception e) {
            log.error("FastAPI 예측 요청 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
