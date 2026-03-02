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
@Schema(description = "AI 랭킹 예측 요청 데이터")
public class AiPredictRequest {

    @Schema(description = "분석할 상품 리스트")
    private List<Item> items;

    @Schema(description = "값 범위를 제한할지 여부", example = "true")
    @JsonProperty("clip_to_range")
    private boolean clipToRange;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "개별 상품 메타데이터")
    public static class Item {
        @Schema(description = "검색 쿼리", example = "가구바퀴")
        private String query;

        @Schema(description = "상품명", example = "무소음 우레탄 가구바퀴 4개입")
        private String title;

        @Schema(description = "최저가", example = "12000")
        private Integer lprice;

        @Schema(description = "최고가", example = "15000")
        private Integer hprice;

        @Schema(description = "몰 이름", example = "지원스토어")
        private String mallName;

        @Schema(description = "브랜드", example = "랭캣가구")
        private String brand;

        @Schema(description = "제조사", example = "랭캣제조")
        private String maker;

        @Schema(description = "대분류", example = "가구/인테리어")
        private String category1;

        @Schema(description = "중분류", example = "DIY자재/용품")
        private String category2;

        @Schema(description = "소분류", example = "가구부속품")
        private String category3;

        @Schema(description = "세분류", example = "가구바퀴")
        private String category4;
    }
}
