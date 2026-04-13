package com.example.RankCat.controller.api;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.auth.WebOAuthSecurityConfig;
import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import java.util.List;
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
        controllers = KeywordToolController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = WebOAuthSecurityConfig.class)
        })
class KeywordToolControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private KeywordToolService keywordToolService;

    @Test
    @DisplayName("파라미터 누락: 추천 키워드 조회 시 hint를 안 보내면 C001 에러가 발생한다")
    void recommend_missingHint_throwsC001() throws Exception {
        mockMvc.perform(get("/api/admin/naver/keyword/recommend"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("정상 조회: 추천 키워드 조회에 성공한다")
    void recommend_success() throws Exception {
        KeywordRecommendResponse response =
                KeywordRecommendResponse.builder()
                        .hint("다이어트")
                        .recommended(List.of("다이어트식단", "다이어트도시락", "단백질쉐이크"))
                        .build();

        given(keywordToolService.recommend(anyString(), anyInt())).willReturn(response);

        mockMvc.perform(
                        get("/api/admin/naver/keyword/recommend")
                                .param("hint", "다이어트")
                                .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hint").value("다이어트"))
                .andExpect(jsonPath("$.recommended[0]").value("다이어트식단"))
                .andExpect(jsonPath("$.recommended[1]").value("다이어트도시락"))
                .andExpect(jsonPath("$.recommended[2]").value("단백질쉐이크"));
    }

    @Test
    @DisplayName("파라미터 누락: 키워드 분석 조회 시 query를 안 보내면 C001 에러가 발생한다")
    void getKeywordAnalysis_missingQuery_throwsC001() throws Exception {
        mockMvc.perform(get("/api/admin/naver/keyword/analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("서비스 비즈니스 에러: 분석 데이터가 없으면 C003 에러가 발생한다")
    void getKeywordAnalysis_notFound_throwsC003() throws Exception {
        given(keywordToolService.getKeywordAnalysis(anyString()))
                .willThrow(new BusinessException(ErrorCode.DATA_NOT_FOUND));

        mockMvc.perform(get("/api/admin/naver/keyword/analysis").param("query", "없는키워드"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("C003"))
                .andExpect(jsonPath("$.message").value("요청하신 데이터를 찾을 수 없습니다."));
    }
}
