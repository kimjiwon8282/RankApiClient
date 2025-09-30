package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.AiPredictRequest;
import com.example.RankCat.dto.ai.AiPredictResponse;
import com.example.RankCat.service.ai.interfaces.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<AiPredictResponse> predict(@RequestBody AiPredictRequest request) {
        AiPredictResponse response = predictionService.predict(request);
        log.info(response.toString());
        return ResponseEntity.ok(response);
    }
}
