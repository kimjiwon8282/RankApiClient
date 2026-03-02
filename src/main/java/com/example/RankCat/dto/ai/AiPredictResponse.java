package com.example.RankCat.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 랭킹 예측 결과 데이터")
public class AiPredictResponse {

    @Schema(description = "상품별 예측 결과 리스트")
    @JsonProperty("results")
    private List<Result> results;

    @Schema(description = "분석된 총 상품 개수", example = "1")
    @JsonProperty("n")
    private Integer n;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "개별 상품 예측 상세 결과")
    public static class Result {

        @Schema(description = "AI가 예측한 예상 랭킹 점수 (소수점 포함)", example = "12.45")
        @JsonProperty("pred_rank")
        private Double predRank;

        @Schema(description = "설정 범위 내로 보정된(Clipped) 예상 랭킹", example = "12.0")
        @JsonProperty("pred_rank_clipped")
        private Double predRankClipped;

        @Schema(description = "분석에 사용된 검색 쿼리", example = "가구바퀴")
        @JsonProperty("query")
        private String query;

        @Schema(description = "분석된 상품명", example = "무소음 우레탄 가구바퀴 4개입")
        @JsonProperty("title")
        private String title;

        @Schema(description = "실험(Experiment) ID 또는 모델 버전 정보", example = "exp_v1_202403")
        @JsonProperty("exp_id")
        private String expId;
    }
}
