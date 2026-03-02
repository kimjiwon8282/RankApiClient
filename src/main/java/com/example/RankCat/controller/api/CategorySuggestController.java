package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.CategorySuggestResponse;
import com.example.RankCat.service.api.interfaces.CategorySuggestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "6. 카테고리 추천", description = "검색어 기반 스마트 카테고리 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategorySuggestController {

    private final CategorySuggestService categorySuggestService;

    /** 예: GET /api/categories/suggest?query=쌈채소&topN=10 */
    @Operation(
            summary = "검색어 기반 카테고리 자동 추천",
            description = "입력된 검색어(query)를 분석하여 가장 적합한 카테고리 경로(CategoryPath)를 제안합니다.")
    @GetMapping("/suggest")
    public ResponseEntity<CategorySuggestResponse> suggest(
            @Parameter(description = "분석할 검색어", example = "쌈채소") @RequestParam String query,
            @Parameter(description = "분석에 사용할 상위 상품 개수 (기본값: 10)", example = "10")
                    @RequestParam(defaultValue = "10")
                    int topN) {

        return ResponseEntity.ok(categorySuggestService.suggestByQuery(query, topN));
    }
}
