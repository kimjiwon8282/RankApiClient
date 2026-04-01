package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.model.SearchAdKeywordResult;

public interface KeywordToolService {
    void collectRelatedKeywords(String hint);

    KeywordRecommendResponse recommend(String hint, int limit);

    SearchAdKeywordResult getKeywordAnalysis(String query);
}
