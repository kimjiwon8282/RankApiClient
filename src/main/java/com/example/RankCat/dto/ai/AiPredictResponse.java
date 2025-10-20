package com.example.RankCat.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPredictResponse {

    @JsonProperty("results")
    private List<Result> results;

    @JsonProperty("n")
    private Integer n;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Result {
        @JsonProperty("pred_rank")
        private Double predRank;

        @JsonProperty("pred_rank_clipped")
        private Double predRankClipped;

        @JsonProperty("query")
        private String query;

        @JsonProperty("title")
        private String title;

        @JsonProperty("exp_id")
        private String expId;
    }
}