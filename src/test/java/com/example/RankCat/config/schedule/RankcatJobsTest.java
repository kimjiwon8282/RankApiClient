package com.example.RankCat.config.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
class RankcatJobsTest {

    @Mock private KeywordToolService keywordToolService;
    @Mock private ShoppingInsightService shoppingInsightService;
    @Mock private CollectorJobService collectorJobService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private RankcatJobs rankcatJobs;

    @Captor private ArgumentCaptor<Long> startedAtCaptor;
    @Captor private ArgumentCaptor<Integer> totalCountCaptor;
    @Captor private ArgumentCaptor<Integer> successCountCaptor;
    @Captor private ArgumentCaptor<List<String>> failedTargetsCaptor;
    @Captor private ArgumentCaptor<String> errorCodeCaptor;
    @Captor private ArgumentCaptor<String> errorMessageCaptor;

    @Test
    @DisplayName("카테고리 트렌드 수집 중 일부 항목이 실패해도 배치를 끝까지 수행하고 실행 이력을 남긴다")
    void collectCategoryTrendDaily_partialFailure_recordsExecutionAndContinues() throws Exception {
        // given
        List<Map<String, String>> categories =
                List.of(
                        Map.of("카테고리번호", "50003308", "세분류", "가구바퀴"),
                        Map.of("카테고리번호", "50002152", "세분류", "메밀"));

        when(objectMapper.readValue(
                        any(InputStream.class),
                        org.mockito.ArgumentMatchers
                                .<TypeReference<List<Map<String, String>>>>any()))
                .thenReturn(categories);

        doNothing()
                .when(shoppingInsightService)
                .collectCategoryTrend(
                        anyString(), anyString(), eq("month"), eq("가구바퀴"), eq("50003308"));

        doThrow(new BusinessException(ErrorCode.EXTERNAL_API_UNAUTHORIZED, "naver auth failed"))
                .when(shoppingInsightService)
                .collectCategoryTrend(
                        anyString(), anyString(), eq("week"), eq("가구바퀴"), eq("50003308"));

        doNothing()
                .when(shoppingInsightService)
                .collectCategoryTrend(
                        anyString(), anyString(), eq("month"), eq("메밀"), eq("50002152"));

        doNothing()
                .when(shoppingInsightService)
                .collectCategoryTrend(
                        anyString(), anyString(), eq("week"), eq("메밀"), eq("50002152"));

        // when
        rankcatJobs.collectCategoryTrendDaily();

        // then: 실패한 항목이 있어도 나머지 항목은 계속 수행되어야 함
        verify(shoppingInsightService, times(4))
                .collectCategoryTrend(
                        anyString(), anyString(), anyString(), anyString(), anyString());

        verify(collectorJobService)
                .recordExecution(
                        eq(CollectorJobNames.CATEGORY_TREND_COLLECT),
                        eq(CollectorTriggerType.SCHEDULED),
                        startedAtCaptor.capture(),
                        totalCountCaptor.capture(),
                        successCountCaptor.capture(),
                        failedTargetsCaptor.capture(),
                        errorCodeCaptor.capture(),
                        errorMessageCaptor.capture());

        assertEquals(4, totalCountCaptor.getValue());
        assertEquals(3, successCountCaptor.getValue());
        assertIterableEquals(List.of("가구바퀴(week)"), failedTargetsCaptor.getValue());
        assertEquals(ErrorCode.EXTERNAL_API_UNAUTHORIZED.getCode(), errorCodeCaptor.getValue());
        assertEquals("naver auth failed", errorMessageCaptor.getValue());
    }

    @Test
    @DisplayName("카테고리 파일 로딩이 실패해도 실행 이력을 남기고 예외를 다시 던진다")
    void collectCategoryTrendDaily_whenCategoryLoadingFails_recordsFailureAndRethrows()
            throws Exception {
        // given
        when(objectMapper.readValue(
                        any(InputStream.class),
                        org.mockito.ArgumentMatchers
                                .<TypeReference<List<Map<String, String>>>>any()))
                .thenThrow(new IOException("broken json"));

        // when
        IOException exception =
                assertThrows(IOException.class, () -> rankcatJobs.collectCategoryTrendDaily());

        // then
        assertEquals("broken json", exception.getMessage());

        verify(collectorJobService)
                .recordExecution(
                        eq(CollectorJobNames.CATEGORY_TREND_COLLECT),
                        eq(CollectorTriggerType.SCHEDULED),
                        startedAtCaptor.capture(),
                        totalCountCaptor.capture(),
                        successCountCaptor.capture(),
                        failedTargetsCaptor.capture(),
                        errorCodeCaptor.capture(),
                        errorMessageCaptor.capture());

        assertEquals(0, totalCountCaptor.getValue());
        assertEquals(0, successCountCaptor.getValue());
        assertIterableEquals(List.of(), failedTargetsCaptor.getValue());
        assertEquals(null, errorCodeCaptor.getValue());
        assertEquals("broken json", errorMessageCaptor.getValue());
    }
}
