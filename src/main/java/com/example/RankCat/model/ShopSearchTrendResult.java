package com.example.RankCat.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shop_search_trend")
@Getter
@Setter
public class ShopSearchTrendResult {
    @Id private String id;
    private List<ShopSearchTrendItem> items;
    private long callAt;
    private long expiresAt;
    private String source;
    private String lastCollectedJob;
}
