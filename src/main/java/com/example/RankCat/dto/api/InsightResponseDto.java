package com.example.RankCat.dto.api;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class InsightResponseDto {
    private final Map<String, Object> monthlyResponse;
    private final Map<String, Object> weeklyResponse;

    @Builder
    public InsightResponseDto(Map<String, Object> monthlyResponse, Map<String, Object> weeklyResponse) {
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