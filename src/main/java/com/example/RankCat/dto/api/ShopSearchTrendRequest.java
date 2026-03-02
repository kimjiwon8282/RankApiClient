package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopSearchTrendRequest {
    @Schema(description = "검색어", example = "쌈채소")
    @NotBlank(message = "검색어를 입력해주세요.")
    private String query;
}
