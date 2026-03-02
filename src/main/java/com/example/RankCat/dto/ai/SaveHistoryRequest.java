package com.example.RankCat.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 분석 이력 저장 요청 객체")
public class SaveHistoryRequest {
    @Schema(description = "검색 쿼리", example = "가구바퀴")
    private String query;

    @Schema(description = "상품 제목", example = "무소음 우레탄 바퀴 4개입")
    private String title;

    @Schema(description = "최저가", example = "12000")
    private Integer lprice;

    @Schema(description = "최고가", example = "15000")
    private Integer hprice;

    @Schema(description = "쇼핑몰 이름", example = "네이버 스마트스토어")
    private String mallName;

    @Schema(description = "브랜드", example = "지완퍼니처")
    private String brand;

    @Schema(description = "제조사", example = "지완공업")
    private String maker;

    @Schema(description = "네이버 상품 고유 ID", example = "123456789")
    private String productId;

    @Schema(description = "상품 타입 (FastAPI 수신값)", example = "2")
    private String productType;

    @Schema(description = "카테고리 1(대)", example = "가구/인테리어")
    private String category1;

    @Schema(description = "카테고리 2(중)", example = "DIY자재/용품")
    private String category2;

    @Schema(description = "카테고리 3(소)", example = "가구부속품")
    private String category3;

    @Schema(description = "카테고리 4(세)", example = "가구바퀴")
    private String category4;

    @Schema(description = "예측 랭킹 점수", example = "15.42")
    private Double predRank;

    @Schema(description = "보정된 예측 랭킹 점수", example = "15.0")
    private Double predRankClipped;
}
