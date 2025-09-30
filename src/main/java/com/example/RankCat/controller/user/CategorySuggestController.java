package com.example.RankCat.controller.user;

import com.example.RankCat.dto.category.CategorySuggestResponse;
import com.example.RankCat.service.api.interfaces.CategorySuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategorySuggestController {

    private final CategorySuggestService categorySuggestService;

    /**
     * 예: GET /api/categories/suggest?query=쌈채소&topN=10
     */
    @GetMapping("/suggest")
    public ResponseEntity<CategorySuggestResponse> suggest(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topN
    ) {
        return ResponseEntity.ok(categorySuggestService.suggestByQuery(query, topN));
    }
}
