package com.example.RankCat.service.schedule;

import com.example.RankCat.service.api.interfaces.KeywordToolService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class NaverSearchAdScheduled {
    private final KeywordToolService keywordToolService;
    private final ObjectMapper objectMapper; //json역 직렬화, 직렬화 설정을 중앙 관리 가능, readValue,writeValue 사용가능

    /**categoryDateVegatable.json에서 세분류 키워드 추출해
    네이버 검색광고 api를 통해 연관 키워드 저잗함.**/
    @Bean
    public ApplicationRunner runOnStartup() {
        return args -> {
            // 정확한 경로로 수정
            var resource = new ClassPathResource("data/categoryDataVegetable.json");
            if (!resource.exists()) {
                throw new IllegalStateException("categoryDataVegetable.json 파일을 찾을 수 없습니다!");
            }

            List<Map<String, String>> categories = objectMapper.readValue(
                    resource.getInputStream(), //파일 바이트 스트림
                    new TypeReference<>() {} //제네릭 타입 유지
            );

            for (Map<String, String> cat : categories) {
                // '세분류'가 없으면 '소분류', '중분류' 순으로 대체
                String combined = cat.getOrDefault("세분류",
                        cat.getOrDefault("소분류", cat.get("중분류")));
                if(combined == null || combined.isBlank()){
                    continue;
                }
                Arrays.stream(combined.split("/"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(keywordToolService::getRelatedKeywords);
            }
        };
    }
}
