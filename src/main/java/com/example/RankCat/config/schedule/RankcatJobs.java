package com.example.RankCat.config.schedule;

import com.example.RankCat.service.api.interfaces.KeywordToolService;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankcatJobs {

    private final KeywordToolService keywordToolService;
    private final ShoppingInsightService shoppingInsightService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- 공통: 카테고리 로딩 + 이름 추출 ---
    private List<Map<String, String>> loadCategories() throws IOException {
        var resource = new ClassPathResource("data/categoryDataVegetable.json");
        if (!resource.exists()) {
            throw new IllegalArgumentException("data/categoryDataVegetable.json not found");
        }
        log.info("카테고리 데이터 파일 로드 완료");
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    private String extractCategoryName(Map<String, String> cat) {
        String combined = cat.get("세분류");
        if (combined == null || combined.isBlank()) combined = cat.get("소분류");
        if (combined == null || combined.isBlank()) combined = cat.get("중분류");
        return (combined == null || combined.isBlank()) ? null : combined.trim();
    }

    // 1) 관련 키워드 수집 — 매일 09:30 KST
    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "rankcat.keyword.collect", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void collectRelatedKeywordsDaily() throws IOException {
        log.info("[JOB] collectRelatedKeywordsDaily: start");
        loadCategories().stream()
                .map(this::extractCategoryName)
                .filter(Objects::nonNull)
                .flatMap(n -> Arrays.stream(n.split("/")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(k -> {
                    try {
                        keywordToolService.getRelatedKeywords(k);
                    } catch (Exception e) {
                        log.warn("keywordTool fail: {} -> {}", k, e.getMessage());
                    }
                });
        log.info("[JOB] collectRelatedKeywordsDaily: done");
    }

    // 2) 쇼핑인사이트(월/주) 카테고리 트렌드 — 매일 09:45 KST
    @Scheduled(cron = "0 45 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "rankcat.insight.category", lockAtMostFor = "PT45M", lockAtLeastFor = "PT2M")
    public void collectCategoryTrendDaily() throws IOException {
        log.info("[JOB] collectCategoryTrendDaily: start");
        var categories = loadCategories();
        LocalDate today   = LocalDate.now();
        String endDate    = today.format(FMT);
        String weekStart  = today.minusWeeks(12).with(java.time.DayOfWeek.MONDAY).format(FMT);
        String monthStart = today.minusYears(1).format(FMT);

        for (var cat : categories) {
            String name = extractCategoryName(cat);
            if (name == null || name.isBlank()) continue;
            String code = cat.get("카테고리번호");
            try {
                shoppingInsightService.getCategoryTrend(monthStart, endDate, "month", name, code);
                shoppingInsightService.getCategoryTrend(weekStart, endDate, "week",  name, code);
            } catch (Exception e) {
                log.warn("insight fail: {}({}) -> {}", name, code, e.getMessage());
            }
        }
        log.info("[JOB] collectCategoryTrendDaily: done");
    }

    // 3) 네이버 쇼핑 검색 트렌드 — 매일 10:00 KST
    @Scheduled(cron = "0 00 10 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "rankcat.shop.search.trend", lockAtMostFor = "PT45M", lockAtLeastFor = "PT2M")
    public void collectShopSearchTrendDaily() throws IOException {
        log.info("[JOB] collectShopSearchTrendDaily: start");
        loadCategories().stream()
                .map(this::extractCategoryName)
                .filter(Objects::nonNull)
                .flatMap(n -> Arrays.stream(n.split("/")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(q -> {
                    try {
                        shoppingInsightService.getShopSearchTrend(q);
                    } catch (Exception e) {
                        log.warn("shopTrend fail: {} -> {}", q, e.getMessage());
                    }
                });
        log.info("[JOB] collectShopSearchTrendDaily: done");
    }
}