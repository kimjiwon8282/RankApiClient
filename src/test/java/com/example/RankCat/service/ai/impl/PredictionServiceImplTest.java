package com.example.RankCat.service.ai.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.ai.AiPredictRequest;
import com.example.RankCat.dto.ai.AiPredictResponse;
import com.example.RankCat.dto.ai.HealthResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock private RestTemplate fastApiRestTemplate;

    @InjectMocks private PredictionServiceImpl predictionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(predictionService, "fastApiUrl", "http://fastapi.internal");
    }

    @Test
    @DisplayName("AI 서버 헬스 체크 성공 시 HealthResponse를 반환한다")
    void checkAiServerHealth_success() {
        // given
        HealthResponse expected = new HealthResponse(true, "models/lgbm-ranker");
        given(
                        fastApiRestTemplate.getForObject(
                                "http://fastapi.internal/health", HealthResponse.class))
                .willReturn(expected);

        // when
        HealthResponse result = predictionService.checkAiServerHealth();

        // then
        assertThat(result).isEqualTo(expected);
        verify(fastApiRestTemplate)
                .getForObject("http://fastapi.internal/health", HealthResponse.class);
    }

    @Test
    @DisplayName("AI 서버 헬스 체크 중 예외가 발생하면 EXTERNAL_API_ERROR를 던진다")
    void checkAiServerHealth_failure_throwsExternalApiError() {
        // given
        given(
                        fastApiRestTemplate.getForObject(
                                "http://fastapi.internal/health", HealthResponse.class))
                .willThrow(new RuntimeException("connection refused"));

        // when & then
        assertThatThrownBy(() -> predictionService.checkAiServerHealth())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
    }

    @Test
    @DisplayName("예측 요청 성공 시 FastAPI 응답을 그대로 반환한다")
    void predict_success() {
        // given
        AiPredictRequest request =
                AiPredictRequest.builder()
                        .items(
                                List.of(
                                        AiPredictRequest.Item.builder()
                                                .query("노트북")
                                                .title("가성비 노트북")
                                                .lprice(1000000)
                                                .hprice(1200000)
                                                .mallName("랭캣스토어")
                                                .brand("RankCat")
                                                .maker("RankCat")
                                                .category1("디지털/가전")
                                                .category2("노트북")
                                                .category3("노트북")
                                                .category4("노트북")
                                                .build()))
                        .clipToRange(true)
                        .build();

        AiPredictResponse expected =
                AiPredictResponse.builder()
                        .n(1)
                        .results(
                                List.of(
                                        AiPredictResponse.Result.builder()
                                                .query("노트북")
                                                .title("가성비 노트북")
                                                .predRank(3.2)
                                                .predRankClipped(3.0)
                                                .expId("exp-202604")
                                                .build()))
                        .build();

        given(
                        fastApiRestTemplate.postForObject(
                                eq("http://fastapi.internal/predict"),
                                eq(request),
                                eq(AiPredictResponse.class)))
                .willReturn(expected);

        // when
        AiPredictResponse result = predictionService.predict(request);

        // then
        assertThat(result).isEqualTo(expected);
        verify(fastApiRestTemplate)
                .postForObject("http://fastapi.internal/predict", request, AiPredictResponse.class);
    }

    @Test
    @DisplayName("예측 요청 중 예외가 발생하면 EXTERNAL_API_ERROR를 던진다")
    void predict_failure_throwsExternalApiError() {
        // given
        AiPredictRequest request =
                AiPredictRequest.builder().items(List.of()).clipToRange(true).build();
        given(
                        fastApiRestTemplate.postForObject(
                                eq("http://fastapi.internal/predict"),
                                any(AiPredictRequest.class),
                                eq(AiPredictResponse.class)))
                .willThrow(new RuntimeException("timeout"));

        // when & then
        assertThatThrownBy(() -> predictionService.predict(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
    }
}
