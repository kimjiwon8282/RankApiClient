package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.dto.api.KeywordRequest;
import com.example.RankCat.model.SearchAdKeywordResult;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. 키워드 도구", description = "네이버 검색광고 API 기반 연관 키워드 및 분석 도구")
@RestController
@RequestMapping("/naver/api/keyword")
@RequiredArgsConstructor
public class KeywordToolController {
    private final KeywordToolService keywordToolService;

    @Operation(summary = "연관 키워드 조회", description = "특정 키워드와 연관된 검색어 목록을 조회합니다.")
    @PostMapping("/")
    public ResponseEntity<?> keywordSearch(@Valid @RequestBody KeywordRequest request) {
        return ResponseEntity.ok(keywordToolService.getRelatedKeywords(request.getKeyword()));
    }

    @Operation(
            summary = "추천 키워드 목록 조회",
            description = "입력한 힌트(hint) 단어를 기반으로 연관 검색어를 N개(limit) 추천받습니다.")
    @GetMapping("/recommend")
    public ResponseEntity<KeywordRecommendResponse> recommend(
            @Parameter(description = "추천 기준이 될 힌트 단어", example = "다이어트") @RequestParam String hint,
            @Parameter(description = "조회할 개수 제한 (기본값: 15)", example = "15")
                    @RequestParam(defaultValue = "15")
                    int limit) {
        return ResponseEntity.ok(keywordToolService.recommend(hint, limit));
    }

    @Operation(
            summary = "키워드 분석 데이터 조회",
            description = "DB에 적재된 특정 키워드의 분석 결과(SearchAdKeywordResult)를 조회합니다.")
    @GetMapping("/analysis")
    public ResponseEntity<SearchAdKeywordResult> getKeywordAnalysis(
            @Parameter(description = "분석을 조회할 쿼리(키워드)", example = "쌈채소") @RequestParam
                    String query) {
        return ResponseEntity.ok(keywordToolService.getKeywordAnalysis(query));
    }
}
