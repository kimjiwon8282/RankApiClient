package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.dto.api.KeywordRequest;
import com.example.RankCat.model.SearchAdKeywordResult;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/naver/api/keyword")
@RequiredArgsConstructor
public class KeywordToolController {
    private final KeywordToolService keywordToolService;

    @PostMapping("/")
    public ResponseEntity<?> keywordSearch(@Valid @RequestBody KeywordRequest request) {
        return ResponseEntity.ok(keywordToolService.getRelatedKeywords(request.getKeyword()));
    }

    @GetMapping("/recommend")
    public ResponseEntity<KeywordRecommendResponse> recommend(
            @RequestParam String hint, @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(keywordToolService.recommend(hint, limit));
    }

    @GetMapping("/analysis")
    public ResponseEntity<SearchAdKeywordResult> getKeywordAnalysis(@RequestParam String query) {
        return ResponseEntity.ok(keywordToolService.getKeywordAnalysis(query));
    }
}
