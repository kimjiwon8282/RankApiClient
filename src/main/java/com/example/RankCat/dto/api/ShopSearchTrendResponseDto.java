package com.example.RankCat.dto.api;

import com.example.RankCat.model.ShopSearchTrendItem;
import com.example.RankCat.model.ShopSearchTrendResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "저장된 쇼핑 검색 트렌드 스냅샷 조회 응답")
public class ShopSearchTrendResponseDto {

    @Schema(description = "검색어", example = "가구바퀴")
    private final String query;

    @Schema(description = "저장된 쇼핑 검색 결과 목록")
    private final List<ShopSearchTrendItem> items;

    @Schema(description = "수집 시각 (Unix Timestamp)", example = "1711977600000")
    private final long callAt;

    @Schema(description = "만료 시각 (Unix Timestamp)", example = "1712064000000")
    private final long expiresAt;

    @Schema(description = "데이터 신선 여부", example = "true")
    private final boolean fresh;

    @Schema(description = "데이터 출처", example = "NAVER_SHOPPING_SEARCH")
    private final String source;

    @Builder
    public ShopSearchTrendResponseDto(
            String query,
            List<ShopSearchTrendItem> items,
            long callAt,
            long expiresAt,
            boolean fresh,
            String source) {
        this.query = query;
        this.items = items;
        this.callAt = callAt;
        this.expiresAt = expiresAt;
        this.fresh = fresh;
        this.source = source;
    }

    public static ShopSearchTrendResponseDto fromEntity(ShopSearchTrendResult entity) {
        if (entity == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return ShopSearchTrendResponseDto.builder()
                .query(entity.getId())
                .items(entity.getItems())
                .callAt(entity.getCallAt())
                .expiresAt(entity.getExpiresAt())
                .fresh(entity.getExpiresAt() > 0 && entity.getExpiresAt() >= now)
                .source(entity.getSource())
                .build();
    }
}
