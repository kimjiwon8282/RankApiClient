//package com.example.RankCat;
//
//import com.example.RankCat.service.api.interfaces.KeywordToolService;
//import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.io.ClassPathResource;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class ApiScheduled {
//    private final KeywordToolService keywordToolService;
//    private final ShoppingInsightService shoppingInsightService;
//    private final ObjectMapper objectMapper;
//    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    // JSON 카테고리 파일 로드
//    private List<Map<String, String>> loadCategories() throws IOException {
//        var resource = new ClassPathResource("data/categoryDataVegetable.json");
//        if (!resource.exists()) {
//            throw new IllegalArgumentException("data/categoryDataVegetable.json not found");
//        }
//        log.info("카테고리 데이터 파일 로드 완료");
//        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>(){});
//    }
//
//    // 카테고리명 추출(세분류 → 소분류 → 중분류 순)
//    private String extractCategoryName(Map<String, String> cat) {
//        String combined = cat.get("세분류");
//        if (combined == null || combined.isBlank()) combined = cat.get("소분류");
//        if (combined == null || combined.isBlank()) combined = cat.get("중분류");
//        return combined != null ? combined.trim() : null;
//    }
//
//    // 관련 키워드 데이터 수집(앱 최초 실행 시)
//    @Bean
//    @Order(1)
//    public ApplicationRunner keywordRunner() {
//        return args -> {
//            List<Map<String, String>> categories = loadCategories();
//            categories.stream()
//                    .map(this::extractCategoryName)
//                    .filter(Objects::nonNull)
//                    .flatMap(combined -> Arrays.stream(combined.split("/")))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .forEach(keyword -> {
//                        log.info("키워드 수집: {}", keyword);
//                        keywordToolService.getRelatedKeywords(keyword);
//                    });
//            log.info("키워드 데이터 수집 완료");
//        };
//    }
//
//    // 쇼핑인사이트 카테고리 트렌드 데이터 수집
//    @Bean
//    @Order(2)
//    public ApplicationRunner insightRunner() {
//        return args -> {
//            List<Map<String,String>> categories = loadCategories();
//
//            LocalDate today      = LocalDate.now();
//            String endDate       = today.format(FMT);
//            String weekStart     = today.minusWeeks(1).format(FMT);
//            String monthStart    = today.minusYears(1).format(FMT);
//
//            for (Map<String, String> cat : categories) {
//                String categoryName = extractCategoryName(cat);
//                if (categoryName == null || categoryName.isBlank()) continue;
//
//                String categoryCode = cat.get("카테고리번호");
//
//                log.info("인사이트 트렌드(월): {}", categoryName);
//                shoppingInsightService.getCategoryTrend(monthStart, endDate, "month", categoryName, categoryCode);
//
//                log.info("인사이트 트렌드(주): {}", categoryName);
//                shoppingInsightService.getCategoryTrend(weekStart, endDate, "week", categoryName, categoryCode);
//            }
//            log.info("쇼핑인사이트 트렌드 데이터 수집 완료");
//        };
//    }
//
//    // 네이버 쇼핑검색 트렌드 데이터 수집
//    @Bean
//    @Order(3)
//    public ApplicationRunner shopSearchTrendRunner() {
//        return args -> {
//            List<Map<String, String>> categories = loadCategories();
//            categories.stream()
//                    .map(this::extractCategoryName)
//                    .filter(Objects::nonNull)
//                    .flatMap(combined -> Arrays.stream(combined.split("/")))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .forEach(query -> {
//                        try {
//                            log.info("쇼핑 트렌드 수집: {}", query);
//                            shoppingInsightService.getShopSearchTrend(query);
//                        } catch (Exception e) {
//                            log.warn("트렌드 저장 실패: {} (사유: {})", query, e.getMessage());
//                        }
//                    });
//            log.info("쇼핑 검색 트렌드 데이터 수집 완료");
//        };
//    }
//}