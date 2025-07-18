package com.example.RankCat.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "shop_search_trend")
@Getter
@Setter
public class ShopSearchTrendResult {
    @Id
    private String id;  // query (검색어)가 PK!
    private List<Map<String, Object>> items; // 상품 전체+rank 포함
    private long callAt;
}