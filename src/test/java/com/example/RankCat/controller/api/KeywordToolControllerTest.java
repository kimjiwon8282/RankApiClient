package com.example.RankCat.controller.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.oauth.WebOAuthSecurityConfig;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * [WebMvcTest] 특정 컨트롤러와 관련된 빈(Bean)들만 로드하여 가볍게 테스트하는 슬라이스 테스트 어노테이션입니다. 1. controllers: 테스트 대상이 되는
 * 컨트롤러를 지정합니다. 2. excludeAutoConfiguration: 테스트 시 불필요한 보안 자동 설정을 꺼서 인증 없이 API를 호출할 수 있게 합니다. 3.
 * excludeFilters: 우리가 직접 작성한 시큐리티 설정(WebOAuthSecurityConfig) 파일도 읽어오지 않도록 배제합니다.
 */
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

    // 브라우저나 포스트맨 대신 가짜 HTTP 요청을 보내주는 도구입니다.
    @Autowired private MockMvc mockMvc;

    // 실제 서비스 대신 가짜(Mock) 서비스를 주입합니다. 실제 DB를 건드리지 않고 우리가 원하는 응답만 주도록 조종합니다.
    @MockBean private KeywordToolService keywordToolService;

    @Test
    @DisplayName("DTO 검증: POST 요청 시 빈 키워드를 보내면 C001 (잘못된 입력값) 에러가 발생한다")
    void keywordSearch_blankKeyword_throwsC001() throws Exception {
        // [Given] 테스트를 준비하는 단계: 빈 키워드가 담긴 가짜 JSON 데이터를 만듭니다.
        String jsonRequest = "{\"keyword\": \"\"}";

        // [When & Then] 실행 및 검증: API를 호출했을 때의 결과를 확인합니다.
        mockMvc.perform(
                        post("/naver/api/keyword/") // POST 방식으로 해당 URL 요청
                                .contentType(MediaType.APPLICATION_JSON) // 데이터 형식은 JSON
                                .content(jsonRequest)) // 우리가 준비한 빈 값 전송
                .andExpect(status().isBadRequest()) // 응답 코드가 400(Bad Request)이어야 함
                .andExpect(jsonPath("$.code").value("C001")); // 응답 결과 JSON의 'code' 값이 'C001'이어야 함
    }

    @Test
    @DisplayName("파라미터 누락: GET 분석 요청 시 파라미터를 안 보내면 C001 에러가 발생한다")
    void getKeywordAnalysis_missingQuery_throwsC001() throws Exception {
        // [When & Then] ?query= 파라미터를 아예 빼고 요청을 보냈을 때의 동작을 확인합니다.
        mockMvc.perform(get("/naver/api/keyword/analysis")) // 파라미터 없이 GET 요청
                .andExpect(status().isBadRequest()) // 필수 파라미터가 없으므로 400 에러 기대
                .andExpect(jsonPath("$.code").value("C001")); // 우리 핸들러가 가공한 에러 코드 'C001' 확인
    }

    @Test
    @DisplayName("서비스 비즈니스 에러: DB에 데이터가 없으면 C003 (데이터 없음) 404 에러가 발생한다")
    void getKeywordAnalysis_notFound_throwsC003() throws Exception {
        // [Given] 가짜 서비스(Mock) 조종: 어떤 문자열이 들어오든(anyString) DATA_NOT_FOUND 예외를 던지도록 설정합니다.
        given(keywordToolService.getKeywordAnalysis(anyString()))
                .willThrow(new BusinessException(ErrorCode.DATA_NOT_FOUND)); // 예외 강제 발생

        // [When & Then] 실제 API 호출 시 위에서 설정한 예외가 GlobalExceptionHandler를 통해 잘 변환되는지 확인합니다.
        mockMvc.perform(get("/naver/api/keyword/analysis").param("query", "없는키워드"))
                .andExpect(status().isNotFound()) // DATA_NOT_FOUND는 404(Not Found) 상태코드를 가짐
                .andExpect(jsonPath("$.code").value("C003")) // 에러 코드는 'C003'
                .andExpect(jsonPath("$.message").value("요청하신 데이터를 찾을 수 없습니다.")); // 메시지 내용까지 검증
    }
}
