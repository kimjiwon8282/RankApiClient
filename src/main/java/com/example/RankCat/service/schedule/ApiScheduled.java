//package com.example.RankCat.service.schedule;
//
//import com.example.RankCat.service.api.interfaces.KeywordToolService;
//import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.io.ClassPathResource;
//
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
//public class ApiScheduled {
//    private final KeywordToolService keywordToolService;
//    private final ShoppingInsightService shoppingInsightService;
//    private final ObjectMapper objectMapper; //json역 직렬화, 직렬화 설정을 중앙 관리 가능, readValue,writeValue 사용가능
//    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    private List<Map<String, String>> loadCategories() throws IOException {
//        var resource = new ClassPathResource("data/categoryDataVegetable.json");
//        if (!resource.exists()) {
//            throw new IllegalArgumentException("data/categoryDataVegetable.json not found");
//        }
//        return objectMapper.readValue(
//                resource.getInputStream(), new TypeReference<>() {
//                }
//        );
//    }
//
//    @Bean
//    @Order(1)
//    public ApplicationRunner keywordRunner() {
//        return args -> {
//            List<Map<String, String>> categories = loadCategories();
//            categories.stream()
//                    .map(cat -> cat.getOrDefault("세분류",
//                            cat.getOrDefault("소분류", cat.get("중분류"))))
//                    .filter(Objects::nonNull)
//                    .flatMap(combined -> Arrays.stream(combined.split("/")))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .forEach(keywordToolService::getRelatedKeywords);
//        };
//    }
//
//    @Bean
//    @Order(2)
//    public ApplicationRunner insightRunner(){
//        return args -> {
//            List<Map<String,String>> categories = loadCategories();
//
//            LocalDate today      = LocalDate.now();
//            String endDate       = today.format(FMT);
//            String weekStart     = today.minusWeeks(1).format(FMT);
//            String monthStart    = today.minusYears(1).format(FMT);
//
//            for (Map<String, String> cat : categories) {
//                String combined = cat.getOrDefault("세분류",
//                        cat.getOrDefault("소분류", cat.get("중분류")));
//                if (combined == null || combined.isBlank()) continue;
//
//                String categoryCode = cat.get("카테고리번호");
//                String categoryName = combined;
//
//                // 월별
//                shoppingInsightService.getCategoryTrend(
//                        monthStart, endDate, "month", categoryName, categoryCode
//                );
//                // 주별
//                shoppingInsightService.getCategoryTrend(
//                        weekStart, endDate, "week", categoryName, categoryCode
//                );
//            }
//        };
//    }
//}