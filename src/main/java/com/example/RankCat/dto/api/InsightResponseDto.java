package com.example.RankCat.dto.api;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "쇼핑 인사이트 카테고리 트렌드 응답 데이터 (DB 조회용)")
public class InsightResponseDto {

    @Schema(description = "월간(Monthly) 트렌드 조회 결과 원본 데이터 (네이버 API 응답 포맷)")
    private final Map<String, Object> monthlyResponse;

    @Schema(description = "주간(Weekly) 트렌드 조회 결과 원본 데이터 (네이버 API 응답 포맷)")
    private final Map<String, Object> weeklyResponse;

    @Builder
    public InsightResponseDto(
            Map<String, Object> monthlyResponse, Map<String, Object> weeklyResponse) {
        this.monthlyResponse = monthlyResponse;
        this.weeklyResponse = weeklyResponse;
    }

    // Entity를 DTO로 변환하는 정적 메소드
    public static InsightResponseDto fromEntity(ShoppingInsightCategoryResult entity) {
        if (entity == null) {
            return null;
        }
        return InsightResponseDto.builder()
                .monthlyResponse(entity.getMonthlyResponse())
                .weeklyResponse(entity.getWeeklyResponse())
                .build();
    }
}
