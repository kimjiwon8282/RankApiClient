package com.example.RankCat.service.ai.interfaces;

import com.example.RankCat.dto.ai.AiPredictRequest;
import com.example.RankCat.dto.ai.AiPredictResponse;
import com.example.RankCat.dto.ai.HealthResponse;

public interface PredictionService {
    public HealthResponse checkAiServerHealth();
    public AiPredictResponse predict(AiPredictRequest request);
}
