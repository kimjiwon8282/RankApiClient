package com.example.RankCat.service.api.impl;

import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.model.ShoppingInsightCategoryResult;
import com.example.RankCat.model.ShoppingInsightKeywordResult;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import com.example.RankCat.repository.ShoppingInsightCategoryRepository;
import com.example.RankCat.repository.ShoppingInsightKeywordRepository;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ShoppingInsightServiceImpl implements ShoppingInsightService {
    private final RestTemplate shoppingInsightRestTemplate;
    private final ShoppingInsightCategoryRepository categoryRepository;
    private final ShoppingInsightKeywordRepository keywordRepository;
    private final ShopSearchTrendResultRepository trendRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCategoryTrend(String startDate, String endDate,
                                                String timeUnit,
                                                String categoryName, String categoryCode) {
        // 요청 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("startDate", startDate);
        body.put("endDate", endDate);
        body.put("timeUnit", timeUnit);
        body.put("category", List.of(
                Map.of("name", categoryName, "param", List.of(categoryCode))
        ));
        body.put("device", "");
        body.put("gender", "");
        body.put("ages", List.of());

        // POST 호출
        Map<String,Object> response = shoppingInsightRestTemplate.postForObject(
                "/v1/datalab/shopping/categories", //URI 템플릿
                new HttpEntity<>(body), //요청 엔티티(헤더+바디)
                Map.class //응답 바디를 매핑할 타입
        );
        log.info("categoryName={},쇼핑인사이트categoryapi 저장 완료",categoryName);

        // 기존 데이터 조회 또는 신규 생성
        ShoppingInsightCategoryResult result = categoryRepository.findById(categoryCode)
                .orElseGet(()->{
                   ShoppingInsightCategoryResult r = new ShoppingInsightCategoryResult();
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

        //저장
        categoryRepository.save(result);
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getKeywordTrend(String startDate, String endDate,
                                               String timeUnit,
                                               String categoryCode, List<String> keywords) {
        // 키워드 리스트를 name/param 구조로 변환
        List<Map<String,Object>> kwList = keywords.stream()
                .map(k -> Map.of("name", k, "param", List.of(k)))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("startDate", startDate);
        body.put("endDate",   endDate);
        body.put("timeUnit",  timeUnit);
        body.put("category",  categoryCode);
        body.put("keyword",   kwList);
        body.put("device",    "");
        body.put("gender",    "");
        body.put("ages",      List.of());

        Map<String,Object> response = shoppingInsightRestTemplate.postForObject(
                "/v1/datalab/shopping/category/keywords",
                new HttpEntity<>(body),
                Map.class
        );
        ShoppingInsightKeywordResult result = new ShoppingInsightKeywordResult();
        List<String> sortedKs = new ArrayList<>(keywords);
        Collections.sort(sortedKs);
        String kwPart = String.join("_", sortedKs);
        result.setId(categoryCode+"_"+kwPart);
        result.setCategoryCode(categoryCode);
        result.setKeywords(keywords);
        result.setResponse(response);
        result.setCallAt(System.currentTimeMillis());

        keywordRepository.save(result);
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getShopSearchTrend(String query) {
        // 네이버 OpenAPI 호출
        String url = "/v1/search/shop?query={query}&display=5";
        Map<String, Object> resp = shoppingInsightRestTemplate
                .getForObject(url, Map.class, query);

        // items에 rank 부여
        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).put("rank", i + 1);
            }
        }

        // MongoDB에 (query가 PK로) upsert (save) 방식 저장
        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId(query);
        doc.setItems(items);
        doc.setCallAt(System.currentTimeMillis());
        trendRepository.save(doc);  // query가 PK니까 덮어씀

        // 로그 및 반환
        log.info("네이버 쇼핑 검색 저장 완료: query={}, items={}", query, items != null ? items.size() : 0);
        return resp;
    }
}
