package com.example.RankCat.dto.api;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySuggestResponse {
    /**
     * 결정 근거:
     * - "rank1": rank=1 아이템의 카테고리
     * - "majority": 상위 N개(기본 10개)의 최빈 경로
     * - "none": 결정 불가
     */
    private String source;
    private Long callAt;                 // shop_search_trend.callAt
    private CategoryPath recommended;    // 자동 채움용 카테고리
}
