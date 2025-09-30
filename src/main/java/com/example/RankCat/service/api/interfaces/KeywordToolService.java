package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.KeywordRecommendResponse;

import java.util.List;
import java.util.Map;


public interface KeywordToolService {
    List<Map<String,Object>> getRelatedKeywords(String hint);
    KeywordRecommendResponse recommend(String hint,int limit);
}
