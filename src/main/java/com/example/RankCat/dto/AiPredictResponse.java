package com.example.RankCat.dto;

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

    private List<Result> results;

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

        private String query;
        private String title;

        @JsonProperty("exp_id")
        private String expId;
    }
}