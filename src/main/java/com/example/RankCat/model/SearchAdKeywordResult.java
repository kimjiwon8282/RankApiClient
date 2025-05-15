package com.example.RankCat.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "search_ad_keywords")
@Getter @Setter
public class SearchAdKeywordResult {

    @Id
    private String keyword;  // hint 값

    private List<Map<String, Object>> relatedKeywords;  // API 응답의 keywordList 그대로 저장
    private long callAt;  // 저장 시각 (timestamp)

}