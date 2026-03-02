package com.example.RankCat.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.config.auth.WebOAuthSecurityConfig;
import com.example.RankCat.service.api.interfaces.CategorySuggestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = CategorySuggestController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = WebOAuthSecurityConfig.class)
        })
class CategorySuggestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CategorySuggestService categorySuggestService;

    @Test
    @DisplayName("파라미터 누락: 추천 카테고리 요청 시 필수 쿼리가 없으면 C001 에러가 발생한다")
    void suggest_missingQuery_throwsC001() throws Exception {
        mockMvc.perform(get("/api/categories/suggest").param("topN", "10")) // query 파라미터를 고의로 누락
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
