package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추천 키워드 목록 응답 데이터")
public class KeywordRecommendResponse {

    @Schema(description = "추천 기준이 된 입력 검색어 (힌트)", example = "다이어트")
    private String hint; // 입력된 검색어

    @Schema(description = "추천된 연관 키워드 목록", example = "[\"다이어트식단\", \"다이어트도시락\", \"단백질쉐이크\"]")
    private List<String> recommended; // 추천 키워드 목록
}
