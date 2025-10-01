package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.InsightResponseDto;

import java.util.List;
import java.util.Map;

public interface ShoppingInsightService {
    /** 카테고리 트렌드 조회 */
    Map<String, Object> getCategoryTrend(
            String startDate, String endDate, String timeUnit,
            String categoryName, String categoryCode);

    /** 카테고리 내 키워드 트렌드 조회 */
    Map<String, Object> getKeywordTrend(
            String startDate, String endDate, String timeUnit,
            String categoryCode, List<String> keywords);

    //검색 api 내 쇼핑
    Map<String, Object> getShopSearchTrend(String query);

    InsightResponseDto getInsightByQuery(String query);
}