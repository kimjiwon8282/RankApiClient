package com.example.RankCat.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "shopping_insight_categories")
@Getter @Setter
public class ShoppingInsightCategoryResult {
    @Id
    private String id; //카테고리 코드

    private String categoryName;
    private Map<String, Object> monthlyResponse;
    private long monthlyCallAt;
    private String startDate_m;             // 요청 시작일
    private String endDate_m;               // 요청 종료일

    private Map<String, Object> weeklyResponse;
    private long weeklyCallAt;
    private String startDate_w;             // 요청 시작일
    private String endDate_w;               // 요청 종료일
}
