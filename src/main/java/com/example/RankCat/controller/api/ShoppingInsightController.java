package com.example.RankCat.controller.api;

import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/naver/api")
public class ShoppingInsightController {
    private final ShoppingInsightService insightService;

    @PostMapping("/insight/categories")
    public ResponseEntity<?> categories(
            @RequestBody Map<String,String> req){
        Map<String,Object> result = insightService.getCategoryTrend(
                req.get("startDate"),
                req.get("endDate"),
                req.get("timeUnit"),
                req.get("categoryName"),
                req.get("categoryCode")
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/insight/keywords")
    public ResponseEntity<?> keywords(
            @RequestBody Map<String,Object> req){
        @SuppressWarnings("unchecked")
        List<String> kws = (List<String>) req.get("keywords");
        Map<String,Object> result = insightService.getKeywordTrend(
                (String) req.get("startDate"),
                (String) req.get("endDate"),
                (String) req.get("timeUnit"),
                (String) req.get("categoryCode"),
                kws
        );
        return ResponseEntity.ok(result);
    }

    // ✅ 네이버 쇼핑 검색(오픈API) 결과에서 items만 반환
    @PostMapping("/trend")
    public ResponseEntity<?> shopSearch(@RequestBody Map<String, String> req) {
        String query = req.get("query");
        // 서비스에서 네이버 응답 전체를 Map으로 반환
        Map<String, Object> resp = insightService.getShopSearchTrend(query);
        // items만 추출해서 반환 (node.js 예제처럼!)
        Object items = resp.get("items");
        return ResponseEntity.ok(items);
    }

}
