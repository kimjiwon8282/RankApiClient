package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryInsightRequest {
    @Schema(description = "조회 시작 일자 (yyyy-mm-dd)", example = "2023-08-01")
    @NotBlank
    private String startDate;

    @Schema(description = "조회 종료 일자 (yyyy-mm-dd)", example = "2023-08-30")
    @NotBlank
    private String endDate;

    @Schema(description = "구간 단위 (date, week, month)", example = "date")
    @NotBlank
    private String timeUnit;

    @Schema(description = "카테고리명 (결과 그래프의 범례로 사용됨)", example = "가구바퀴")
    @NotBlank
    private String categoryName;

    @Schema(description = "네이버 쇼핑 카테고리 코드", example = "50003308")
    @NotBlank
    private String categoryCode;
}
