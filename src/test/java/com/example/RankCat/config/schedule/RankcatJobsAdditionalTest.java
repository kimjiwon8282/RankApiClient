package com.example.RankCat.config.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.model.CollectorTriggerType;
import com.example.RankCat.service.api.interfaces.CollectorJobService;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import com.example.RankCat.service.api.interfaces.ShoppingInsightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankcatJobsAdditionalTest {

    @Mock private KeywordToolService keywordToolService;
    @Mock private ShoppingInsightService shoppingInsightService;
    @Mock private CollectorJobService collectorJobService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private RankcatJobs rankcatJobs;

    @Captor private ArgumentCaptor<Integer> totalCountCaptor;
    @Captor private ArgumentCaptor<Integer> successCountCaptor;
    @Captor private ArgumentCaptor<List<String>> failedTargetsCaptor;
    @Captor private ArgumentCaptor<String> errorCodeCaptor;
    @Captor private ArgumentCaptor<String> errorMessageCaptor;

    @Test
    @DisplayName("연관 키워드 수집 중 일부 키워드가 실패해도 남은 키워드는 계속 처리하고 실행 이력을 남긴다")
    void collectRelatedKeywordsDaily_partialFailure_recordsExecutionAndContinues()
            throws Exception {
        // given
        List<Map<String, String>> categories =
                List.of(
                        Map.of("카테고리번호", "1", "세분류", "가구바퀴/우레탄바퀴"),
                        Map.of("카테고리번호", "2", "세분류", "가구바퀴/메밀"));

        when(objectMapper.readValue(
                        any(InputStream.class),
                        org.mockito.ArgumentMatchers
                                .<TypeReference<List<Map<String, String>>>>any()))
                .thenReturn(categories);

        doNothing().when(keywordToolService).collectRelatedKeywords("가구바퀴");
        doThrow(new BusinessException(ErrorCode.EXTERNAL_API_TIMEOUT, "timeout"))
                .when(keywordToolService)
                .collectRelatedKeywords("우레탄바퀴");
        doNothing().when(keywordToolService).collectRelatedKeywords("메밀");

        // when
        rankcatJobs.collectRelatedKeywordsDaily();

        // then
        verify(keywordToolService, times(1)).collectRelatedKeywords("가구바퀴");
        verify(keywordToolService, times(1)).collectRelatedKeywords("우레탄바퀴");
        verify(keywordToolService, times(1)).collectRelatedKeywords("메밀");

        verify(collectorJobService)
                .recordExecution(
                        eq(CollectorJobNames.RELATED_KEYWORD_COLLECT),
                        eq(CollectorTriggerType.SCHEDULED),
                        any(Long.class),
                        totalCountCaptor.capture(),
                        successCountCaptor.capture(),
                        failedTargetsCaptor.capture(),
                        errorCodeCaptor.capture(),
                        errorMessageCaptor.capture());

        assertThat(totalCountCaptor.getValue()).isEqualTo(3);
        assertThat(successCountCaptor.getValue()).isEqualTo(2);
        assertThat(failedTargetsCaptor.getValue()).containsExactly("우레탄바퀴");
        assertThat(errorCodeCaptor.getValue()).isEqualTo(ErrorCode.EXTERNAL_API_TIMEOUT.getCode());
        assertThat(errorMessageCaptor.getValue()).isEqualTo("timeout");
    }

    @Test
    @DisplayName("쇼핑 검색 수집 중 전체 로딩이 실패하면 실행 이력을 남기고 예외를 다시 던진다")
    void collectShopSearchTrendDaily_whenCategoryLoadingFails_recordsFailureAndRethrows()
            throws Exception {
        // given
        when(objectMapper.readValue(
                        any(InputStream.class),
                        org.mockito.ArgumentMatchers
                                .<TypeReference<List<Map<String, String>>>>any()))
                .thenThrow(new IOException("broken json"));

        // when
        IOException exception =
                assertThrows(IOException.class, () -> rankcatJobs.collectShopSearchTrendDaily());

        // then
        assertThat(exception.getMessage()).isEqualTo("broken json");
        verify(collectorJobService)
                .recordExecution(
                        eq(CollectorJobNames.SHOP_SEARCH_TREND_COLLECT),
                        eq(CollectorTriggerType.SCHEDULED),
                        any(Long.class),
                        totalCountCaptor.capture(),
                        successCountCaptor.capture(),
                        failedTargetsCaptor.capture(),
                        errorCodeCaptor.capture(),
                        errorMessageCaptor.capture());

        assertThat(totalCountCaptor.getValue()).isZero();
        assertThat(successCountCaptor.getValue()).isZero();
        assertThat(failedTargetsCaptor.getValue()).isEmpty();
        assertThat(errorCodeCaptor.getValue()).isNull();
        assertThat(errorMessageCaptor.getValue()).isEqualTo("broken json");
    }

    @Test
    @DisplayName("쇼핑 검색 수집 중 일부 검색어가 실패해도 나머지는 계속 처리하고 비즈니스 에러 코드를 기록한다")
    void collectShopSearchTrendDaily_partialFailure_recordsBusinessErrorCode() throws Exception {
        // given
        List<Map<String, String>> categories =
                List.of(
                        Map.of("카테고리번호", "1", "세분류", "가구바퀴/우레탄바퀴"),
                        Map.of("카테고리번호", "2", "세분류", "메밀"));

        when(objectMapper.readValue(
                        any(InputStream.class),
                        org.mockito.ArgumentMatchers
                                .<TypeReference<List<Map<String, String>>>>any()))
                .thenReturn(categories);

        doNothing().when(shoppingInsightService).collectShopSearchTrend("가구바퀴");
        doThrow(new BusinessException(ErrorCode.EXTERNAL_API_SERVER_ERROR, "server error"))
                .when(shoppingInsightService)
                .collectShopSearchTrend("우레탄바퀴");
        doNothing().when(shoppingInsightService).collectShopSearchTrend("메밀");

        // when
        rankcatJobs.collectShopSearchTrendDaily();

        // then
        verify(shoppingInsightService, times(1)).collectShopSearchTrend("가구바퀴");
        verify(shoppingInsightService, times(1)).collectShopSearchTrend("우레탄바퀴");
        verify(shoppingInsightService, times(1)).collectShopSearchTrend("메밀");
        verify(collectorJobService)
                .recordExecution(
                        eq(CollectorJobNames.SHOP_SEARCH_TREND_COLLECT),
                        eq(CollectorTriggerType.SCHEDULED),
                        any(Long.class),
                        totalCountCaptor.capture(),
                        successCountCaptor.capture(),
                        failedTargetsCaptor.capture(),
                        errorCodeCaptor.capture(),
                        errorMessageCaptor.capture());

        assertThat(totalCountCaptor.getValue()).isEqualTo(3);
        assertThat(successCountCaptor.getValue()).isEqualTo(2);
        assertThat(failedTargetsCaptor.getValue()).containsExactly("우레탄바퀴");
        assertThat(errorCodeCaptor.getValue())
                .isEqualTo(ErrorCode.EXTERNAL_API_SERVER_ERROR.getCode());
        assertThat(errorMessageCaptor.getValue()).isEqualTo("server error");
    }
}
