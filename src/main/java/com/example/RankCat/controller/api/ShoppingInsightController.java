package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.CategoryInsightRequest;
import com.example.RankCat.dto.api.KeywordInsightRequest;
import com.example.RankCat.dto.api.ShopSearchTrendRequest;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. 쇼핑 인사이트", description = "네이버 데이터랩 쇼핑 인사이트 API 연동")
@RestController
@RequiredArgsConstructor
@RequestMapping("/naver/api")
@Validated
public class ShoppingInsightController {
    private final ShoppingInsightService insightService;

    @Operation(summary = "카테고리별 트렌드 조회", description = "특정 카테고리의 기간별 검색 트렌드를 조회합니다.")
    @PostMapping("/insight/categories")
    public ResponseEntity<?> categories(@Valid @RequestBody CategoryInsightRequest req) {
        Map<String, Object> result =
                insightService.getCategoryTrend(
                        req.getStartDate(),
                        req.getEndDate(),
                        req.getTimeUnit(),
                        req.getCategoryName(),
                        req.getCategoryCode());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "키워드별 트렌드 조회", description = "특정 카테고 내에서 입력한 키워드들의 트렌드를 비교 조회합니다.")
    @PostMapping("/insight/keywords")
    public ResponseEntity<?> keywords(@Valid @RequestBody KeywordInsightRequest req) {
        Map<String, Object> result =
                insightService.getKeywordTrend(
                        req.getStartDate(),
                        req.getEndDate(),
                        req.getTimeUnit(),
                        req.getCategoryCode(),
                        req.getKeywords());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "쇼핑 검색 트렌드 조회 (아이템 반환)",
            description = "네이버 쇼핑 검색 오픈 API 결과를 호출하여 아이템 목록만 반환합니다.")
    @PostMapping("/trend")
    public ResponseEntity<?> shopSearch(@Valid @RequestBody ShopSearchTrendRequest req) {
        Map<String, Object> resp = insightService.getShopSearchTrend(req.getQuery());
        Object items = resp.get("items");
        return ResponseEntity.ok(items);
    }

    @Operation(
            summary = "카테고리 트렌드 데이터 조회 (DB)",
            description = "DB에 저장된 특정 키워드/카테고리의 트렌드 분석 결과를 조회합니다.")
    @GetMapping("/category-trend")
    public ResponseEntity<?> getCategoryTrend(
            @Parameter(description = "조회할 검색어", example = "쌈채소")
                    @RequestParam
                    @NotBlank(message = "쿼리를 입력해주세요.")
                    String query) {
        return ResponseEntity.ok(insightService.getInsightByQuery(query));
    }
}
