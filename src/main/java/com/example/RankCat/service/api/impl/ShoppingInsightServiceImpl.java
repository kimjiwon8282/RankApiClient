package com.example.RankCat.service.api.impl;

import com.example.RankCat.client.naver.NaverShopSearchClient;
import com.example.RankCat.client.naver.NaverShoppingInsightClient;
import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.api.InsightResponseDto;
import com.example.RankCat.dto.api.KeywordTrendResponseDto;
import com.example.RankCat.dto.api.ShopSearchTrendResponseDto;
import com.example.RankCat.model.ShopSearchTrendItem;
import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.model.ShoppingInsightCategoryResult;
import com.example.RankCat.model.ShoppingInsightKeywordResult;
import com.example.RankCat.model.ShoppingInsightTrendResponse;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import com.example.RankCat.repository.ShoppingInsightCategoryRepository;
import com.example.RankCat.repository.ShoppingInsightKeywordRepository;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ShoppingInsightServiceImpl implements ShoppingInsightService {
    private final NaverShoppingInsightClient shoppingInsightClient;
    private final NaverShopSearchClient shopSearchClient;
    private final ShoppingInsightCategoryRepository categoryRepository;
    private final ShoppingInsightKeywordRepository keywordRepository;
    private final ShopSearchTrendResultRepository trendRepository;

    @Override
    public void collectCategoryTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryName,
            String categoryCode) {
        ShoppingInsightTrendResponse response =
                shoppingInsightClient.fetchCategoryTrend(
                        startDate, endDate, timeUnit, categoryName, categoryCode);

        ShoppingInsightCategoryResult result =
                categoryRepository
                        .findById(categoryCode)
                        .orElseGet(
                                () -> {
                                    ShoppingInsightCategoryResult r =
                                            new ShoppingInsightCategoryResult();
                                    r.setId(categoryCode);
                                    r.setCategoryName(categoryName);
                                    return r;
                                });

        long now = System.currentTimeMillis();
        if ("month".equalsIgnoreCase(timeUnit)) {
            result.setMonthlyResponse(response);
            result.setMonthlyCallAt(now);
            result.setStartDate_m(startDate);
            result.setEndDate_m(endDate);
        } else if ("week".equalsIgnoreCase(timeUnit)) {
            result.setWeeklyResponse(response);
            result.setWeeklyCallAt(now);
            result.setStartDate_w(startDate);
            result.setEndDate_w(endDate);
        }

        categoryRepository.save(result);
        log.info("categoryName={}, 쇼핑인사이트 category api 저장 완료", categoryName);
    }

    @Override
    public void collectKeywordTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryCode,
            List<String> keywords) {
        ShoppingInsightTrendResponse response =
                shoppingInsightClient.fetchKeywordTrend(
                        startDate, endDate, timeUnit, categoryCode, keywords);

        ShoppingInsightKeywordResult result = new ShoppingInsightKeywordResult();
        List<String> sortedKeywords = new ArrayList<>(keywords);
        Collections.sort(sortedKeywords);
        result.setId(buildKeywordTrendId(categoryCode, sortedKeywords));
        result.setCategoryCode(categoryCode);
        result.setKeywords(keywords);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTimeUnit(timeUnit);
        result.setResponse(response);
        result.setCallAt(System.currentTimeMillis());

        keywordRepository.save(result);
        log.info("categoryCode={}, keywords={} 쇼핑인사이트 keyword api 저장 완료", categoryCode, keywords);
    }

    @Override
    public void collectShopSearchTrend(String query) {
        NaverShopSearchClient.NaverShopSearchResponse page1 =
                shopSearchClient.search(query, 1, 100);
        List<ShopSearchTrendItem> items1 = defaultItems(page1.getItems());
        int total = page1.getTotal() > 0 ? page1.getTotal() : items1.size();

        List<ShopSearchTrendItem> items2 = List.of();
        if (total > 100) {
            NaverShopSearchClient.NaverShopSearchResponse page2 =
                    shopSearchClient.search(query, 101, 100);
            items2 = defaultItems(page2.getItems());
        }

        LinkedHashMap<String, ShopSearchTrendItem> uniqueItems = new LinkedHashMap<>();
        addUniqueItems(uniqueItems, items1);
        addUniqueItems(uniqueItems, items2);

        List<ShopSearchTrendItem> merged = new ArrayList<>(uniqueItems.values());
        if (merged.size() > 200) {
            merged = new ArrayList<>(merged.subList(0, 200));
        }

        for (int i = 0; i < merged.size(); i++) {
            merged.get(i).setRank(i + 1);
        }

        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId(query);
        doc.setItems(merged);
        doc.setCallAt(System.currentTimeMillis());
        trendRepository.save(doc);

        log.info(
                "네이버 쇼핑 검색 저장 완료: query={}, total={}, items(page1)={}, items(page2)={}, merged={}",
                query,
                total,
                items1.size(),
                items2.size(),
                merged.size());
    }

    @Override
    public InsightResponseDto getInsightByQuery(String query) {
        ShoppingInsightCategoryResult insight =
                categoryRepository
                        .findByCategoryName(query)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
        return InsightResponseDto.fromEntity(insight);
    }

    @Override
    public KeywordTrendResponseDto getKeywordTrendSnapshot(
            String categoryCode, List<String> keywords) {
        List<String> sortedKeywords = new ArrayList<>(keywords);
        Collections.sort(sortedKeywords);
        String id = buildKeywordTrendId(categoryCode, sortedKeywords);
        ShoppingInsightKeywordResult result =
                keywordRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
        return KeywordTrendResponseDto.fromEntity(result);
    }

    @Override
    public ShopSearchTrendResponseDto getShopSearchTrendSnapshot(String query) {
        ShopSearchTrendResult result =
                trendRepository
                        .findById(query)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
        return ShopSearchTrendResponseDto.fromEntity(result);
    }

    private String buildKeywordTrendId(String categoryCode, List<String> sortedKeywords) {
        return categoryCode + "_" + String.join("_", sortedKeywords);
    }

    private List<ShopSearchTrendItem> defaultItems(List<ShopSearchTrendItem> items) {
        return items != null ? items : List.of();
    }

    private void addUniqueItems(
            LinkedHashMap<String, ShopSearchTrendItem> uniqueItems,
            List<ShopSearchTrendItem> sourceItems) {
        for (ShopSearchTrendItem item : sourceItems) {
            uniqueItems.putIfAbsent(resolveItemKey(item), item);
        }
    }

    private String resolveItemKey(ShopSearchTrendItem item) {
        String productId = trimToNull(item.getProductId());
        if (productId != null) {
            return productId;
        }
        return String.join(
                "|",
                defaultString(item.getTitle()),
                defaultString(item.getMallName()),
                defaultString(item.getLink()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
