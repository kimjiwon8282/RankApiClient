package com.example.RankCat.service.api.impl;

import com.example.RankCat.dto.api.CategoryPath;
import com.example.RankCat.dto.api.CategorySuggestResponse;
import com.example.RankCat.model.ShopSearchTrendItem;
import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import com.example.RankCat.service.api.interfaces.CategorySuggestService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        List<ShopSearchTrendItem> items = Optional.ofNullable(doc.getItems()).orElse(List.of());

        ShopSearchTrendItem rank1 =
                items.stream().filter(item -> rankOf(item) == 1).findFirst().orElse(null);

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

        List<ShopSearchTrendItem> top =
                items.stream()
                        .sorted(Comparator.comparingInt(this::rankOf))
                        .limit(Math.max(1, topN))
                        .collect(Collectors.toList());

        Map<CategoryPath, Long> freq =
                top.stream()
                        .map(this::toCategoryPath)
                        .filter(this::isComplete)
                        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        if (!freq.isEmpty()) {
            CategoryPath majority =
                    freq.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);

            return CategorySuggestResponse.builder()
                    .source("majority")
                    .callAt(doc.getCallAt())
                    .recommended(majority)
                    .build();
        }

        return CategorySuggestResponse.builder()
                .source("none")
                .callAt(doc.getCallAt())
                .recommended(null)
                .build();
    }

    private int rankOf(ShopSearchTrendItem item) {
        return item.getRank() != null ? item.getRank() : Integer.MAX_VALUE;
    }

    private CategoryPath toCategoryPath(ShopSearchTrendItem item) {
        return CategoryPath.builder()
                .category1(item.getCategory1())
                .category2(item.getCategory2())
                .category3(item.getCategory3())
                .category4(item.getCategory4())
                .build();
    }

    private boolean isComplete(CategoryPath cp) {
        return cp != null
                && nonEmpty(cp.getCategory1())
                && nonEmpty(cp.getCategory2())
                && nonEmpty(cp.getCategory3())
                && nonEmpty(cp.getCategory4());
    }

    private boolean nonEmpty(String s) {
        return s != null && !s.isBlank();
    }
}
