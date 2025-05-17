package com.example.RankCat.service.api.impl;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import com.example.RankCat.model.ShoppingInsightKeywordResult;
import com.example.RankCat.repository.ShoppingInsightCategoryRepository;
import com.example.RankCat.repository.ShoppingInsightKeywordRepository;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ShoppingInsightServiceImpl implements ShoppingInsightService {
    private final RestTemplate shoppingInsightRestTemplate;
    private final ShoppingInsightCategoryRepository categoryRepository;
    private final ShoppingInsightKeywordRepository keywordRepository;

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

        // 저장
        ShoppingInsightCategoryResult result = new ShoppingInsightCategoryResult();
        result.setId(categoryCode);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTimeUnit(timeUnit);
        result.setCategoryName(categoryName);
        result.setResponse(response);
        result.setCallAt(System.currentTimeMillis());

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
}
