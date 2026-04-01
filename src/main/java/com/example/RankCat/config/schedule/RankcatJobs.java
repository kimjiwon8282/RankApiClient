package com.example.RankCat.config.schedule;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.model.CollectorTriggerType;
import com.example.RankCat.service.api.interfaces.CollectorJobService;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankcatJobs {

    private final KeywordToolService keywordToolService;
    private final ShoppingInsightService shoppingInsightService;
    private final CollectorJobService collectorJobService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    private List<String> extractDistinctKeywords(List<Map<String, String>> categories) {
        return categories.stream()
                .map(this::extractCategoryName)
                .filter(Objects::nonNull)
                .flatMap(n -> Arrays.stream(n.split("/")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                                ArrayList::new));
    }

    private List<CategoryTarget> extractCategoryTargets(List<Map<String, String>> categories) {
        List<CategoryTarget> targets = new ArrayList<>();
        for (Map<String, String> cat : categories) {
            String name = extractCategoryName(cat);
            String code = cat.get("카테고리번호");
            if (name == null || name.isBlank() || code == null || code.isBlank()) {
                continue;
            }
            targets.add(new CategoryTarget(name, code));
        }
        return targets;
    }

    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = CollectorJobNames.RELATED_KEYWORD_COLLECT,
            lockAtMostFor = "PT30M",
            lockAtLeastFor = "PT1M")
    public void collectRelatedKeywordsDaily() throws IOException {
        long startedAt = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        List<String> failedTargets = new ArrayList<>();
        String errorCode = null;
        String errorMessage = null;

        try {
            log.info("[JOB] collectRelatedKeywordsDaily: start");
            List<String> keywords = extractDistinctKeywords(loadCategories());
            totalCount = keywords.size();

            for (String keyword : keywords) {
                try {
                    keywordToolService.collectRelatedKeywords(keyword);
                    successCount++;
                } catch (Exception e) {
                    failedTargets.add(keyword);
                    errorCode = resolveErrorCode(e);
                    errorMessage = e.getMessage();
                    log.warn("keywordTool fail: {} -> {}", keyword, e.getMessage());
                }
            }
            log.info("[JOB] collectRelatedKeywordsDaily: done");
        } catch (Exception e) {
            errorCode = resolveErrorCode(e);
            errorMessage = e.getMessage();
            throw e;
        } finally {
            collectorJobService.recordExecution(
                    CollectorJobNames.RELATED_KEYWORD_COLLECT,
                    CollectorTriggerType.SCHEDULED,
                    startedAt,
                    totalCount,
                    successCount,
                    failedTargets,
                    errorCode,
                    errorMessage);
        }
    }

    @Scheduled(cron = "0 45 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = CollectorJobNames.CATEGORY_TREND_COLLECT,
            lockAtMostFor = "PT45M",
            lockAtLeastFor = "PT2M")
    public void collectCategoryTrendDaily() throws IOException {
        long startedAt = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        List<String> failedTargets = new ArrayList<>();
        String errorCode = null;
        String errorMessage = null;

        try {
            log.info("[JOB] collectCategoryTrendDaily: start");
            List<CategoryTarget> targets = extractCategoryTargets(loadCategories());
            totalCount = targets.size() * 2;
            LocalDate today = LocalDate.now();
            String endDate = today.format(FMT);
            String weekStart = today.minusWeeks(12).with(java.time.DayOfWeek.MONDAY).format(FMT);
            String monthStart = today.minusYears(1).format(FMT);

            for (CategoryTarget target : targets) {
                try {
                    shoppingInsightService.collectCategoryTrend(
                            monthStart, endDate, "month", target.name(), target.code());
                    successCount++;
                } catch (Exception e) {
                    failedTargets.add(target.name() + "(month)");
                    errorCode = resolveErrorCode(e);
                    errorMessage = e.getMessage();
                    log.warn(
                            "insight monthly fail: {}({}) -> {}",
                            target.name(),
                            target.code(),
                            e.getMessage());
                }

                try {
                    shoppingInsightService.collectCategoryTrend(
                            weekStart, endDate, "week", target.name(), target.code());
                    successCount++;
                } catch (Exception e) {
                    failedTargets.add(target.name() + "(week)");
                    errorCode = resolveErrorCode(e);
                    errorMessage = e.getMessage();
                    log.warn(
                            "insight weekly fail: {}({}) -> {}",
                            target.name(),
                            target.code(),
                            e.getMessage());
                }
            }
            log.info("[JOB] collectCategoryTrendDaily: done");
        } catch (Exception e) {
            errorCode = resolveErrorCode(e);
            errorMessage = e.getMessage();
            throw e;
        } finally {
            collectorJobService.recordExecution(
                    CollectorJobNames.CATEGORY_TREND_COLLECT,
                    CollectorTriggerType.SCHEDULED,
                    startedAt,
                    totalCount,
                    successCount,
                    failedTargets,
                    errorCode,
                    errorMessage);
        }
    }

    @Scheduled(cron = "0 00 10 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = CollectorJobNames.SHOP_SEARCH_TREND_COLLECT,
            lockAtMostFor = "PT45M",
            lockAtLeastFor = "PT2M")
    public void collectShopSearchTrendDaily() throws IOException {
        long startedAt = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        List<String> failedTargets = new ArrayList<>();
        String errorCode = null;
        String errorMessage = null;

        try {
            log.info("[JOB] collectShopSearchTrendDaily: start");
            List<String> queries = extractDistinctKeywords(loadCategories());
            totalCount = queries.size();

            for (String query : queries) {
                try {
                    shoppingInsightService.collectShopSearchTrend(query);
                    successCount++;
                } catch (Exception e) {
                    failedTargets.add(query);
                    errorCode = resolveErrorCode(e);
                    errorMessage = e.getMessage();
                    log.warn("shopTrend fail: {} -> {}", query, e.getMessage());
                }
            }
            log.info("[JOB] collectShopSearchTrendDaily: done");
        } catch (Exception e) {
            errorCode = resolveErrorCode(e);
            errorMessage = e.getMessage();
            throw e;
        } finally {
            collectorJobService.recordExecution(
                    CollectorJobNames.SHOP_SEARCH_TREND_COLLECT,
                    CollectorTriggerType.SCHEDULED,
                    startedAt,
                    totalCount,
                    successCount,
                    failedTargets,
                    errorCode,
                    errorMessage);
        }
    }

    private String resolveErrorCode(Exception e) {
        if (e instanceof BusinessException businessException) {
            return businessException.getErrorCode().getCode();
        }
        return null;
    }

    private record CategoryTarget(String name, String code) {}
}
