package com.example.RankCat.controller.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.auth.WebOAuthSecurityConfig;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
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
        controllers = ShoppingInsightController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = WebOAuthSecurityConfig.class)
        })
class ShoppingInsightControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ShoppingInsightService insightService;

    @Test
    @DisplayName("단일 파라미터 공백: 카테고리 트렌드 조회 시 빈칸을 보내면 C001 에러가 발생한다")
    void getCategoryTrend_blankQuery_throwsC001() throws Exception {
        mockMvc.perform(get("/naver/api/category-trend").param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("외부 API 에러: 쇼핑 검색 트렌드 조회 시 외부 API가 터지면 E001 에러가 발생한다")
    void getShopTrend_apiError_throwsE001() throws Exception {
        given(insightService.getShopSearchTrendSnapshot(anyString()))
                .willThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        mockMvc.perform(get("/naver/api/shop-trend").param("query", "노트북"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("E001"))
                .andExpect(jsonPath("$.message").value("외부 API 연동 중 문제가 발생했습니다."));
    }
}
