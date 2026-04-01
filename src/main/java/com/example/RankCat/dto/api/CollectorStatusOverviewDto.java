package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "수집 잡 상태 개요 응답")
public class CollectorStatusOverviewDto {

    @Schema(description = "상태 확인 시각 (Unix Timestamp)", example = "1711978000000")
    private final long checkedAt;

    private final CollectorJobStatusDto relatedKeywordJob;
    private final CollectorJobStatusDto categoryTrendJob;
    private final CollectorJobStatusDto shopSearchTrendJob;

    @Builder
    public CollectorStatusOverviewDto(
            long checkedAt,
            CollectorJobStatusDto relatedKeywordJob,
            CollectorJobStatusDto categoryTrendJob,
            CollectorJobStatusDto shopSearchTrendJob) {
        this.checkedAt = checkedAt;
        this.relatedKeywordJob = relatedKeywordJob;
        this.categoryTrendJob = categoryTrendJob;
        this.shopSearchTrendJob = shopSearchTrendJob;
    }
}
