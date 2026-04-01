package com.example.RankCat.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shopping_insight_keywords")
@Getter
@Setter
public class ShoppingInsightKeywordResult {
    @Id private String id;

    private String categoryCode;
    private List<String> keywords;
    private String startDate;
    private String endDate;
    private String timeUnit;
    private ShoppingInsightTrendResponse response;
    private long callAt;
    private long expiresAt;
    private String source;
    private String lastCollectedJob;
}
