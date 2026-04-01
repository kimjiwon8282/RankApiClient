package com.example.RankCat.dto.api;

import com.example.RankCat.model.ShoppingInsightKeywordResult;
import com.example.RankCat.model.ShoppingInsightTrendResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "저장된 키워드 트렌드 스냅샷 조회 응답")
public class KeywordTrendResponseDto {

    @Schema(description = "문서 식별자", example = "50003308_우레탄바퀴_캐스터")
    private final String id;

    @Schema(description = "카테고리 코드", example = "50003308")
    private final String categoryCode;

    @Schema(description = "수집에 사용한 키워드 목록")
    private final List<String> keywords;

    @Schema(description = "조회 시작일", example = "2025-01-01")
    private final String startDate;

    @Schema(description = "조회 종료일", example = "2025-03-31")
    private final String endDate;

    @Schema(description = "집계 단위", example = "week")
    private final String timeUnit;

    @Schema(description = "키워드 트렌드 응답 스냅샷")
    private final ShoppingInsightTrendResponse response;

    @Schema(description = "수집 시각 (Unix Timestamp)", example = "1711977600000")
    private final long callAt;

    @Schema(description = "만료 시각 (Unix Timestamp)", example = "1712582400000")
    private final long expiresAt;

    @Schema(description = "데이터 신선 여부", example = "true")
    private final boolean fresh;

    @Schema(description = "데이터 출처", example = "NAVER_DATALAB_SHOPPING")
    private final String source;

    @Builder
    public KeywordTrendResponseDto(
            String id,
            String categoryCode,
            List<String> keywords,
            String startDate,
            String endDate,
            String timeUnit,
            ShoppingInsightTrendResponse response,
            long callAt,
            long expiresAt,
            boolean fresh,
            String source) {
        this.id = id;
        this.categoryCode = categoryCode;
        this.keywords = keywords;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeUnit = timeUnit;
        this.response = response;
        this.callAt = callAt;
        this.expiresAt = expiresAt;
        this.fresh = fresh;
        this.source = source;
    }

    public static KeywordTrendResponseDto fromEntity(ShoppingInsightKeywordResult entity) {
        if (entity == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return KeywordTrendResponseDto.builder()
                .id(entity.getId())
                .categoryCode(entity.getCategoryCode())
                .keywords(entity.getKeywords())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .timeUnit(entity.getTimeUnit())
                .response(entity.getResponse())
                .callAt(entity.getCallAt())
                .expiresAt(entity.getExpiresAt())
                .fresh(entity.getExpiresAt() > 0 && entity.getExpiresAt() >= now)
                .source(entity.getSource())
                .build();
    }
}
