package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.InsightResponseDto;
import com.example.RankCat.dto.api.KeywordTrendResponseDto;
import com.example.RankCat.dto.api.ShopSearchTrendResponseDto;
import java.util.List;

public interface ShoppingInsightService {
    void collectCategoryTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryName,
            String categoryCode);

    void collectKeywordTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryCode,
            List<String> keywords);

    void collectShopSearchTrend(String query);

    InsightResponseDto getInsightByQuery(String query);

    KeywordTrendResponseDto getKeywordTrendSnapshot(String categoryCode, List<String> keywords);

    ShopSearchTrendResponseDto getShopSearchTrendSnapshot(String query);
}
