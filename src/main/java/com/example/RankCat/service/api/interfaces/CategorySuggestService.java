package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.category.CategorySuggestResponse;

public interface CategorySuggestService {
    CategorySuggestResponse suggestByQuery(String query, int topN);
}
