package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.model.SearchAdKeywordResult;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/naver/api/keyword")
@RequiredArgsConstructor
public class KeywordToolController {
    private final KeywordToolService keywordToolService;

    @PostMapping("/")
    public ResponseEntity<?> keywordSearch(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("키워드를 입력하세요.");
        }
        try {
            return ResponseEntity.ok(
                    keywordToolService.getRelatedKeywords(keyword));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("API 호출 실패: " + e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public ResponseEntity<KeywordRecommendResponse> recommend(
            @RequestParam String hint,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(keywordToolService.recommend(hint, limit));
    }

    @GetMapping("/analysis")
    public ResponseEntity<SearchAdKeywordResult> getKeywordAnalysis(@RequestParam String query) {
        // 서비스의 getKeywordAnalysis 메소드를 호출합니다.
        Optional<SearchAdKeywordResult> resultOpt = keywordToolService.getKeywordAnalysis(query);

        // Optional을 사용하여 코드를 간결하게 만듭니다.
        // 데이터가 있으면(isPresent), map 내부 로직을 실행하여 200 OK 응답을 보냅니다.
        // 데이터가 없으면(isEmpty), orElseGet 내부 로직을 실행하여 404 Not Found 응답을 보냅니다.
        return resultOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
