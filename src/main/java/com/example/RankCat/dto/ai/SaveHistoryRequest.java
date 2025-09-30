package com.example.RankCat.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveHistoryRequest {
    // 요청
    private String query;
    private String title;
    private Integer lprice;
    private Integer hprice;
    private String mallName;
    private String brand;
    private String maker;
    private String productId;
    private String productType; // FastAPI에서 문자열 "2" 형태
    private String category1;
    private String category2;
    private String category3;
    private String category4;

    // 응답
    private Double predRank;
    private Double predRankClipped;
}
