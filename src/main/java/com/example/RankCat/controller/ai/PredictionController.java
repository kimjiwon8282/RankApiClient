package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.AiPredictRequest;
import com.example.RankCat.dto.ai.AiPredictResponse;
import com.example.RankCat.dto.ai.HealthResponse;
import com.example.RankCat.service.ai.interfaces.PredictionService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. AI 서비스", description = "AI 모델 서버(FastAPI) 연동 및 예측 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class PredictionController {

    private final PredictionService predictionService;

    @Hidden // 👈 코드는 유지하되 Swagger 명세서에서는 숨깁니다.
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        return ResponseEntity.ok(predictionService.checkAiServerHealth());
    }

    @Operation(
            summary = "상품 랭킹 예측 실행",
            description = "네이버 쇼핑 검색 결과 아이템 리스트를 기반으로 AI 모델이 랭킹을 예측합니다. **(인증 필수)**")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예측 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 누락)"),
        @ApiResponse(responseCode = "500", description = "AI 모델 서버(FastAPI) 연동 실패")
    })
    @PostMapping("/predict")
    public ResponseEntity<AiPredictResponse> predict(@Valid @RequestBody AiPredictRequest request) {
        AiPredictResponse response = predictionService.predict(request);
        log.info(response.toString());
        return ResponseEntity.ok(response);
    }
}
