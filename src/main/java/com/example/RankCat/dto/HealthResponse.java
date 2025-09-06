package com.example.RankCat.dto;

public record HealthResponse(
        boolean ok,
        String model_dir
) {}