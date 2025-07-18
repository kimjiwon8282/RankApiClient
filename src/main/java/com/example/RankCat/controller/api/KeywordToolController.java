package com.example.RankCat.controller.api;

import com.example.RankCat.service.api.interfaces.KeywordToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KeywordToolController {
    private final KeywordToolService keywordToolService;

    @PostMapping("/naver/api/keyword")
    public ResponseEntity<?> keywordSearch(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("키워드를 입력하세요.");
        }
        try {
            return ResponseEntity.ok(
                    keywordToolService.getRelatedKeywords(keyword));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("API 호출 실패: " + e.getMessage());
        }
    }
}
