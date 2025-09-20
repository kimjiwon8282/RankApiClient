package com.example.RankCat.service.ai.interfaces;

import com.example.RankCat.dto.AiPredictRequest;
import com.example.RankCat.dto.AiPredictResponse;
import com.example.RankCat.dto.HealthResponse;

public interface PredictionService {
    public HealthResponse checkAiServerHealth();
    public AiPredictResponse predict(AiPredictRequest request);
}
