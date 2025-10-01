package com.example.RankCat.service.api.impl;

import com.example.RankCat.dto.api.CategoryPath;
import com.example.RankCat.dto.api.CategorySuggestResponse;
import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import com.example.RankCat.service.api.interfaces.CategorySuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategorySuggestServiceImpl implements CategorySuggestService {

    private final ShopSearchTrendResultRepository repo;

    @Override
    public CategorySuggestResponse suggestByQuery(String query, int topN) {
        Optional<ShopSearchTrendResult> opt = repo.findById(query);
        if (opt.isEmpty()) {
            return CategorySuggestResponse.builder()
                    .source("none")
                    .callAt(null)
                    .recommended(null)
                    .build();
        }

        ShopSearchTrendResult doc = opt.get();
        List<Map<String, Object>> items = Optional.ofNullable(doc.getItems()).orElse(List.of());

        // 1) rank=1 우선
        Map<String, Object> rank1 = items.stream()
                .filter(m -> Objects.equals(asInt(m.get("rank")), 1))
                .findFirst()
                .orElse(null);

        if (rank1 != null) {
            CategoryPath cp = toCategoryPath(rank1);
            if (isComplete(cp)) {
                return CategorySuggestResponse.builder()
                        .source("rank1")
                        .callAt(doc.getCallAt())
                        .recommended(cp)
                        .build();
            }
        }

        // 2) 상위 N(기본 10) 최빈 경로
        List<Map<String, Object>> top = items.stream()
                .sorted(Comparator.comparingInt(m -> asInt(m.get("rank"))))
                .limit(Math.max(1, topN))
                .collect(Collectors.toList());

        Map<CategoryPath, Long> freq = top.stream()
                .map(this::toCategoryPath)
                .filter(this::isComplete)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        if (!freq.isEmpty()) {
            CategoryPath majority = freq.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            return CategorySuggestResponse.builder()
                    .source("majority")
                    .callAt(doc.getCallAt())
                    .recommended(majority)
                    .build();
        }

        // 3) 실패
        return CategorySuggestResponse.builder()
                .source("none")
                .callAt(doc.getCallAt())
                .recommended(null)
                .build();
    }

    private int asInt(Object o) {
        if (o == null) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private CategoryPath toCategoryPath(Map<String, Object> m) {
        return CategoryPath.builder()
                .category1(asStr(m.get("category1")))
                .category2(asStr(m.get("category2")))
                .category3(asStr(m.get("category3")))
                .category4(asStr(m.get("category4")))
                .build();
    }

    private String asStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private boolean isComplete(CategoryPath cp) {
        return cp != null &&
                nonEmpty(cp.getCategory1()) &&
                nonEmpty(cp.getCategory2()) &&
                nonEmpty(cp.getCategory3()) &&
                nonEmpty(cp.getCategory4());
    }

    private boolean nonEmpty(String s) {
        return s != null && !s.isBlank();
    }
}