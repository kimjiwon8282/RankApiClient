package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "검색어 기반 카테고리 자동 추천 응답 데이터")
public class CategorySuggestResponse {

    @Schema(
            description = "결정 근거 ('rank1': 1위 상품 기준, 'majority': 최빈값, 'none': 결정 불가)",
            example = "majority")
    private String source;

    @Schema(description = "데이터 갱신 시각 (Unix Timestamp)", example = "1693526400000")
    private Long callAt; // shop_search_trend.callAt

    @Schema(description = "자동 채움용 카테고리 객체 (source가 'none'일 경우 null 가능)")
    private CategoryPath recommended; // 자동 채움용 카테고리
}
