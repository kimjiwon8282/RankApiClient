package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordInsightRequest {
    @Schema(description = "조회 시작 일자 (yyyy-mm-dd)", example = "2023-08-01")
    @NotBlank
    private String startDate;

    @Schema(description = "조회 종료 일자 (yyyy-mm-dd)", example = "2023-08-30")
    @NotBlank
    private String endDate;

    @Schema(description = "구간 단위 (date, week, month)", example = "date")
    @NotBlank
    private String timeUnit;

    @Schema(description = "네이버 쇼핑 카테고리 코드", example = "50003308")
    @NotBlank
    private String categoryCode;

    @Schema(
            description = "해당 카테고리 내에서 트렌드를 비교할 검색 키워드 목록 (최대 5개)",
            example = "[\"우레탄바퀴\", \"캐스터\"]")
    @NotEmpty
    private List<String> keywords;
}
