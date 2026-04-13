package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.InsightResponseDto;
import com.example.RankCat.dto.api.KeywordTrendResponseDto;
import com.example.RankCat.dto.api.ShopSearchTrendResponseDto;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. 쇼핑 인사이트", description = "스케줄러가 저장한 쇼핑 인사이트/쇼핑 검색 결과 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/naver")
@Validated
public class ShoppingInsightController {
    private final ShoppingInsightService insightService;

    @Operation(
            summary = "카테고리 트렌드 데이터 조회 (DB)",
            description = "스케줄러가 저장한 특정 검색어/카테고리의 카테고리 트렌드 스냅샷을 조회합니다.")
    @GetMapping("/category-trend")
    public ResponseEntity<InsightResponseDto> getCategoryTrend(
            @Parameter(description = "조회할 검색어", example = "쌈채소")
                    @RequestParam
                    @NotBlank(message = "쿼리를 입력해주세요.")
                    String query) {
        return ResponseEntity.ok(insightService.getInsightByQuery(query));
    }

    @Operation(
            summary = "키워드 트렌드 데이터 조회 (DB)",
            description = "스케줄러 또는 내부 수집 로직이 저장한 키워드 트렌드 스냅샷을 조회합니다.")
    @GetMapping("/keyword-trend")
    public ResponseEntity<KeywordTrendResponseDto> getKeywordTrend(
            @Parameter(description = "카테고리 코드", example = "50003308")
                    @RequestParam
                    @NotBlank(message = "카테고리 코드를 입력해주세요.")
                    String categoryCode,
            @Parameter(description = "조회할 키워드 목록", example = "우레탄바퀴")
                    @RequestParam
                    @NotEmpty(message = "키워드 목록을 입력해주세요.")
                    List<String> keywords) {
        return ResponseEntity.ok(insightService.getKeywordTrendSnapshot(categoryCode, keywords));
    }

    @Operation(
            summary = "쇼핑 검색 트렌드 데이터 조회 (DB)",
            description = "스케줄러가 저장한 특정 검색어의 쇼핑 검색 결과 스냅샷을 조회합니다.")
    @GetMapping("/shop-trend")
    public ResponseEntity<ShopSearchTrendResponseDto> getShopTrend(
            @Parameter(description = "조회할 검색어", example = "가구바퀴")
                    @RequestParam
                    @NotBlank(message = "쿼리를 입력해주세요.")
                    String query) {
        return ResponseEntity.ok(insightService.getShopSearchTrendSnapshot(query));
    }
}
