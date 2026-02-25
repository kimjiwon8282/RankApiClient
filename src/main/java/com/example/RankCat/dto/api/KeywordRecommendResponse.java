package com.example.RankCat.dto.api;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordRecommendResponse {
    private String hint; // 입력된 검색어
    private List<String> recommended; // 추천 키워드 목록
}
