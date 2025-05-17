package com.example.RankCat.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "shopping_insight_keywords")
@Getter @Setter
public class ShoppingInsightKeywordResult {
    @Id
    private String id;

    private String categoryCode;          // 카테고리 코드
    private List<String> keywords;        // 요청한 키워드 리스트

    private String startDate;             // 요청 시작일
    private String endDate;               // 요청 종료일
    private String timeUnit;              // 시간 단위 (daily, weekly 등)

    private Map<String, Object> response; // API 호출 결과 전체
    private long callAt;                  // 저장 시각 (timestamp)
}
