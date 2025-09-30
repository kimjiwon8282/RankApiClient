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
public class AiPredictRequest {

    private List<Item> items;

    @JsonProperty("clip_to_range")
    private boolean clipToRange;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String query;
        private String title;
        private Integer lprice;
        private Integer hprice;
        private String mallName;
        private String brand;
        private String maker;
        private String productId;
        private String productType;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
    }
}
