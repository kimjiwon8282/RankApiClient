package com.example.RankCat.service.api.interfaces;

import java.util.List;
import java.util.Map;


public interface KeywordToolService {
    List<Map<String,Object>> getRelatedKeywords(String hint);
}
