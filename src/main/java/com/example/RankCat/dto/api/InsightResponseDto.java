package com.example.RankCat.dto.api;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import com.example.RankCat.model.ShoppingInsightTrendResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "저장된 카테고리 트렌드 스냅샷 조회 응답")
public class InsightResponseDto {

    @Schema(description = "카테고리 코드", example = "50003308")
    private final String categoryCode;

    @Schema(description = "카테고리 이름", example = "가구바퀴")
    private final String categoryName;

    @Schema(description = "월간 트렌드 응답 스냅샷")
    private final ShoppingInsightTrendResponse monthlyResponse;

    @Schema(description = "주간 트렌드 응답 스냅샷")
    private final ShoppingInsightTrendResponse weeklyResponse;

    @Schema(description = "월간 수집 시각 (Unix Timestamp)", example = "1711977600000")
    private final long monthlyCallAt;

    @Schema(description = "주간 수집 시각 (Unix Timestamp)", example = "1711977600000")
    private final long weeklyCallAt;

    @Schema(description = "월간 만료 시각 (Unix Timestamp)", example = "1714569600000")
    private final long monthlyExpiresAt;

    @Schema(description = "주간 만료 시각 (Unix Timestamp)", example = "1712582400000")
    private final long weeklyExpiresAt;

    @Schema(description = "월간 데이터 신선 여부", example = "true")
    private final boolean monthlyFresh;

    @Schema(description = "주간 데이터 신선 여부", example = "true")
    private final boolean weeklyFresh;

    @Schema(description = "월간 데이터 출처", example = "NAVER_DATALAB_SHOPPING")
    private final String monthlySource;

    @Schema(description = "주간 데이터 출처", example = "NAVER_DATALAB_SHOPPING")
    private final String weeklySource;

    @Builder
    public InsightResponseDto(
            String categoryCode,
            String categoryName,
            ShoppingInsightTrendResponse monthlyResponse,
            ShoppingInsightTrendResponse weeklyResponse,
            long monthlyCallAt,
            long weeklyCallAt,
            long monthlyExpiresAt,
            long weeklyExpiresAt,
            boolean monthlyFresh,
            boolean weeklyFresh,
            String monthlySource,
            String weeklySource) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.monthlyResponse = monthlyResponse;
        this.weeklyResponse = weeklyResponse;
        this.monthlyCallAt = monthlyCallAt;
        this.weeklyCallAt = weeklyCallAt;
        this.monthlyExpiresAt = monthlyExpiresAt;
        this.weeklyExpiresAt = weeklyExpiresAt;
        this.monthlyFresh = monthlyFresh;
        this.weeklyFresh = weeklyFresh;
        this.monthlySource = monthlySource;
        this.weeklySource = weeklySource;
    }

    public static InsightResponseDto fromEntity(ShoppingInsightCategoryResult entity) {
        if (entity == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return InsightResponseDto.builder()
                .categoryCode(entity.getId())
                .categoryName(entity.getCategoryName())
                .monthlyResponse(entity.getMonthlyResponse())
                .weeklyResponse(entity.getWeeklyResponse())
                .monthlyCallAt(entity.getMonthlyCallAt())
                .weeklyCallAt(entity.getWeeklyCallAt())
                .monthlyExpiresAt(entity.getMonthlyExpiresAt())
                .weeklyExpiresAt(entity.getWeeklyExpiresAt())
                .monthlyFresh(
                        entity.getMonthlyExpiresAt() > 0 && entity.getMonthlyExpiresAt() >= now)
                .weeklyFresh(entity.getWeeklyExpiresAt() > 0 && entity.getWeeklyExpiresAt() >= now)
                .monthlySource(entity.getMonthlySource())
                .weeklySource(entity.getWeeklySource())
                .build();
    }
}
