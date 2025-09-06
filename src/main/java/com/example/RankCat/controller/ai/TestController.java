package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.HealthResponse;
import com.example.RankCat.service.ai.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final PredictionService predictionService;
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    public TestController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/test-ai-health")
    public HealthResponse testAiServerConnection() {
        log.info("AI 서버 헬스 체크를 시작합니다...");
        HealthResponse response = predictionService.checkAiServerHealth();
        log.info("AI 서버 응답: {}", response);
        return response;
    }
}
