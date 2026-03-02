package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KeywordRequest {
    @Schema(description = "검색 및 분석할 키워드", example = "쌈채소")
    @NotBlank(message = "키워드를 입력하세요.")
    private String keyword;
}
