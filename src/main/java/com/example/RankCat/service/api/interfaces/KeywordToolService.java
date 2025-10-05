package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.model.SearchAdKeywordResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface KeywordToolService {
    List<Map<String,Object>> getRelatedKeywords(String hint);
    KeywordRecommendResponse recommend(String hint,int limit);
    Optional<SearchAdKeywordResult> getKeywordAnalysis(String query);
}
