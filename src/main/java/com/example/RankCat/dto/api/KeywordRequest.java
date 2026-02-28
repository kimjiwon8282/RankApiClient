package com.example.RankCat.dto.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KeywordRequest {
    @NotBlank(message = "키워드를 입력하세요.")
    private String keyword;
}
