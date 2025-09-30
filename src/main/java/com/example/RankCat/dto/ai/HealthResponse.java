package com.example.RankCat.dto.ai;

public record HealthResponse(
        boolean ok,
        String model_dir
) {}