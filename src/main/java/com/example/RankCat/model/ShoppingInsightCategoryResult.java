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
    private String id;

    private String startDate;
    private String endDate;
    private String timeUnit;
    private String categoryName;

    private Map<String,Object> response;
    private long callAt;
}
