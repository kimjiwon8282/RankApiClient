package com.example.RankCat.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shopping_insight_categories")
@Getter
@Setter
public class ShoppingInsightCategoryResult {
    @Id private String id;

    private String categoryName;
    private ShoppingInsightTrendResponse monthlyResponse;
    private long monthlyCallAt;
    private String startDate_m;
    private String endDate_m;
    private long monthlyExpiresAt;
    private String monthlySource;
    private String monthlyLastCollectedJob;

    private ShoppingInsightTrendResponse weeklyResponse;
    private long weeklyCallAt;
    private String startDate_w;
    private String endDate_w;
    private long weeklyExpiresAt;
    private String weeklySource;
    private String weeklyLastCollectedJob;
}
