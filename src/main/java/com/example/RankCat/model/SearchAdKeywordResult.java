package com.example.RankCat.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "search_ad_keywords")
@Getter
@Setter
public class SearchAdKeywordResult {
    @Id private String keyword;

    private List<SearchAdKeywordItem> relatedKeywords;
    private long callAt;
    private long expiresAt;
    private String source;
    private String lastCollectedJob;
}
