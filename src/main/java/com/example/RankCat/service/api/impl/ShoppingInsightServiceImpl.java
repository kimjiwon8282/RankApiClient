package com.example.RankCat.service.api.impl;

import com.example.RankCat.dto.api.InsightResponseDto;
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

        // 1) 1페이지(1~100)
        Map<String, Object> resp1 = fetchShopPage(query, 1, 100);
        List<Map<String, Object>> items1 = (List<Map<String, Object>>) resp1.getOrDefault("items", List.of());
        int total = ((Number) resp1.getOrDefault("total", items1.size())).intValue();

        // 2) 2페이지(101~200) — total이 100 초과할 때만 호출(불필요 호출 방지)
        List<Map<String, Object>> items2 = List.of();
        if (total > 100) {
            Map<String, Object> resp2 = fetchShopPage(query, 101, 100);
            items2 = (List<Map<String, Object>>) resp2.getOrDefault("items", List.of());
        }

        // 3) 합치고 productId 중복 제거(LinkedHashMap으로 순서 유지)
        Map<String, Map<String, Object>> uniq = new LinkedHashMap<>();
        for (Map<String, Object> it : items1) {
            String key = String.valueOf(it.get("productId")); // productId가 항상 온다고 가정
            uniq.putIfAbsent(key, it);
        }
        for (Map<String, Object> it : items2) {
            String key = String.valueOf(it.get("productId"));
            uniq.putIfAbsent(key, it);
        }
        List<Map<String, Object>> merged = new ArrayList<>(uniq.values());

        // 4) 200개 초과시 컷(안전)
        if (merged.size() > 200) {
            merged = merged.subList(0, 200);
        }

        // 5) 최종 rank 재부여(1..N)
        for (int i = 0; i < merged.size(); i++) {
            merged.get(i).put("rank", i + 1);
        }

        // 6) MongoDB 저장 (query를 PK로 덮어쓰기: 히스토리 보존하려면 PK를 query+timestamp로 바꾸세요)
        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId(query);
        doc.setItems(merged);
        doc.setCallAt(System.currentTimeMillis());
        trendRepository.save(doc);

        log.info("네이버 쇼핑 검색 저장 완료: query={}, total={}, items(page1)={}, items(page2)={}, merged={}",
                query, total, items1.size(), items2.size(), merged.size());

        // 7) 반환: 2페이지까지 합친 결과를 응답 형태로 구성
        Map<String, Object> out = new HashMap<>(resp1);
        out.put("display", merged.size());
        out.put("start", 1);
        out.put("items", merged);
        return out;
    }

    /**
     * 페이지 호출 헬퍼: display<=100, start는 1-based
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchShopPage(String query, int start, int display) {
        String url = "/v1/search/shop?query={query}&display={display}&start={start}";
        Map<String, Object> uriVars = new HashMap<>();
        uriVars.put("query", query);
        uriVars.put("display", Math.min(display, 100)); // 안전: 최대 100
        uriVars.put("start", start);

        Map<String, Object> resp = shoppingInsightRestTemplate.getForObject(url, Map.class, uriVars);
        if (resp == null) {
            resp = new HashMap<>();
            resp.put("items", List.of());
            resp.put("total", 0);
        }
        return resp;
    }

    @Override
    public InsightResponseDto getInsightByQuery(String query) {
        // 1. 쿼리(검색어)를 categoryName으로 간주하고 DB에서 직접 조회합니다.
        Optional<ShoppingInsightCategoryResult> insightOpt = categoryRepository.findByCategoryName(query);

        if (insightOpt.isEmpty()) {
            log.warn("카테고리명 '{}'에 대한 쇼핑 인사이트 데이터가 없습니다.", query);
            return null;
        }

        // 2. 조회된 데이터를 DTO로 변환하여 반환합니다.
        return InsightResponseDto.fromEntity(insightOpt.get());
    }
}
