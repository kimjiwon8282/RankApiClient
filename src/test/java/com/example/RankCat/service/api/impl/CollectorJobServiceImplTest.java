package com.example.RankCat.service.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.schedule.CollectorJobNames;
import com.example.RankCat.dto.api.CollectorJobExecutionDto;
import com.example.RankCat.dto.api.CollectorStatusOverviewDto;
import com.example.RankCat.model.CollectorJobExecution;
import com.example.RankCat.model.CollectorJobStatus;
import com.example.RankCat.model.CollectorTriggerType;
import com.example.RankCat.repository.CollectorJobExecutionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CollectorJobServiceImplTest {

    @Mock private CollectorJobExecutionRepository collectorJobExecutionRepository;

    @InjectMocks private CollectorJobServiceImpl collectorJobService;

    @Test
    @DisplayName("실행 이력 저장 시 실패 대상이 없으면 SUCCESS 상태와 빈 실패 목록으로 기록한다")
    void recordExecution_success_recordsSuccessStatus() {
        // given
        long startedAt = System.currentTimeMillis() - 500L;
        given(collectorJobExecutionRepository.save(any(CollectorJobExecution.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        collectorJobService.recordExecution(
                CollectorJobNames.RELATED_KEYWORD_COLLECT,
                CollectorTriggerType.SCHEDULED,
                startedAt,
                5,
                5,
                null,
                null,
                null);

        // then
        ArgumentCaptor<CollectorJobExecution> captor =
                ArgumentCaptor.forClass(CollectorJobExecution.class);
        verify(collectorJobExecutionRepository).save(captor.capture());

        CollectorJobExecution saved = captor.getValue();
        assertThat(saved.getJobName()).isEqualTo(CollectorJobNames.RELATED_KEYWORD_COLLECT);
        assertThat(saved.getTriggerType()).isEqualTo(CollectorTriggerType.SCHEDULED);
        assertThat(saved.getTotalCount()).isEqualTo(5);
        assertThat(saved.getSuccessCount()).isEqualTo(5);
        assertThat(saved.getFailCount()).isZero();
        assertThat(saved.getFailedTargets()).isEmpty();
        assertThat(saved.getStatus()).isEqualTo(CollectorJobStatus.SUCCESS);
        assertThat(saved.getDurationMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("실패 대상이 일부 있으면 PARTIAL_SUCCESS 상태로 기록한다")
    void recordExecution_partialFailure_recordsPartialSuccessStatus() {
        // given
        long startedAt = System.currentTimeMillis() - 500L;
        given(collectorJobExecutionRepository.save(any(CollectorJobExecution.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        collectorJobService.recordExecution(
                CollectorJobNames.CATEGORY_TREND_COLLECT,
                CollectorTriggerType.MANUAL,
                startedAt,
                5,
                3,
                List.of("keyword-a", "keyword-b"),
                "E001",
                "external api error");

        // then
        ArgumentCaptor<CollectorJobExecution> captor =
                ArgumentCaptor.forClass(CollectorJobExecution.class);
        verify(collectorJobExecutionRepository).save(captor.capture());

        CollectorJobExecution saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CollectorJobStatus.PARTIAL_SUCCESS);
        assertThat(saved.getFailCount()).isEqualTo(2);
        assertThat(saved.getFailedTargets()).containsExactly("keyword-a", "keyword-b");
        assertThat(saved.getErrorCode()).isEqualTo("E001");
        assertThat(saved.getErrorMessage()).isEqualTo("external api error");
    }

    @Test
    @DisplayName("최근 실행 이력 조회 시 limit은 최대 100으로 보정하고 DTO로 변환한다")
    void getRecentExecutions_normalizesLimitAndMapsDtos() {
        // given
        CollectorJobExecution execution = new CollectorJobExecution();
        execution.setJobName(CollectorJobNames.SHOP_SEARCH_TREND_COLLECT);
        execution.setStatus(CollectorJobStatus.SUCCESS);
        execution.setTriggerType(CollectorTriggerType.SCHEDULED);
        execution.setStartedAt(1000L);
        execution.setFinishedAt(1300L);
        execution.setDurationMs(300L);
        execution.setTotalCount(10);
        execution.setSuccessCount(10);
        execution.setFailCount(0);
        execution.setFailedTargets(List.of());

        given(collectorJobExecutionRepository.findAllByOrderByStartedAtDesc(any(Pageable.class)))
                .willReturn(List.of(execution));

        // when
        List<CollectorJobExecutionDto> result = collectorJobService.getRecentExecutions(1000);

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(collectorJobExecutionRepository)
                .findAllByOrderByStartedAtDesc(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobName())
                .isEqualTo(CollectorJobNames.SHOP_SEARCH_TREND_COLLECT);
        assertThat(result.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(result.get(0).getTriggerType()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("최신 실행 이력이 없으면 DATA_NOT_FOUND 예외를 던진다")
    void getLatestExecution_withoutData_throwsDataNotFound() {
        // given
        given(
                        collectorJobExecutionRepository.findFirstByJobNameOrderByStartedAtDesc(
                                CollectorJobNames.RELATED_KEYWORD_COLLECT))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                        () ->
                                collectorJobService.getLatestExecution(
                                        CollectorJobNames.RELATED_KEYWORD_COLLECT))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    @Test
    @DisplayName("상태 개요 조회 시 최신 실행과 마지막 성공 시각을 잡별로 조합한다")
    void getStatusOverview_buildsOverviewFromLatestAndLastSuccess() {
        // given
        CollectorJobExecution relatedLatest =
                execution(
                        CollectorJobNames.RELATED_KEYWORD_COLLECT,
                        CollectorJobStatus.PARTIAL_SUCCESS,
                        1000L,
                        1500L,
                        10,
                        8,
                        2,
                        "E001",
                        "external api error");
        CollectorJobExecution relatedLastSuccess =
                execution(
                        CollectorJobNames.RELATED_KEYWORD_COLLECT,
                        CollectorJobStatus.SUCCESS,
                        500L,
                        900L,
                        10,
                        10,
                        0,
                        null,
                        null);

        CollectorJobExecution categoryLatest =
                execution(
                        CollectorJobNames.CATEGORY_TREND_COLLECT,
                        CollectorJobStatus.SUCCESS,
                        2000L,
                        2300L,
                        5,
                        5,
                        0,
                        null,
                        null);

        given(
                        collectorJobExecutionRepository.findFirstByJobNameOrderByStartedAtDesc(
                                CollectorJobNames.RELATED_KEYWORD_COLLECT))
                .willReturn(Optional.of(relatedLatest));
        given(
                        collectorJobExecutionRepository
                                .findFirstByJobNameAndStatusOrderByFinishedAtDesc(
                                        CollectorJobNames.RELATED_KEYWORD_COLLECT,
                                        CollectorJobStatus.SUCCESS))
                .willReturn(Optional.of(relatedLastSuccess));

        given(
                        collectorJobExecutionRepository.findFirstByJobNameOrderByStartedAtDesc(
                                CollectorJobNames.CATEGORY_TREND_COLLECT))
                .willReturn(Optional.of(categoryLatest));
        given(
                        collectorJobExecutionRepository
                                .findFirstByJobNameAndStatusOrderByFinishedAtDesc(
                                        CollectorJobNames.CATEGORY_TREND_COLLECT,
                                        CollectorJobStatus.SUCCESS))
                .willReturn(Optional.of(categoryLatest));

        given(
                        collectorJobExecutionRepository.findFirstByJobNameOrderByStartedAtDesc(
                                CollectorJobNames.SHOP_SEARCH_TREND_COLLECT))
                .willReturn(Optional.empty());
        given(
                        collectorJobExecutionRepository
                                .findFirstByJobNameAndStatusOrderByFinishedAtDesc(
                                        CollectorJobNames.SHOP_SEARCH_TREND_COLLECT,
                                        CollectorJobStatus.SUCCESS))
                .willReturn(Optional.empty());

        // when
        CollectorStatusOverviewDto overview = collectorJobService.getStatusOverview();

        // then
        assertThat(overview.getCheckedAt()).isGreaterThan(0L);

        assertThat(overview.getRelatedKeywordJob().getJobName())
                .isEqualTo(CollectorJobNames.RELATED_KEYWORD_COLLECT);
        assertThat(overview.getRelatedKeywordJob().getLatestStatus()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(overview.getRelatedKeywordJob().getLatestStartedAt()).isEqualTo(1000L);
        assertThat(overview.getRelatedKeywordJob().getLatestFinishedAt()).isEqualTo(1500L);
        assertThat(overview.getRelatedKeywordJob().getLastSuccessAt()).isEqualTo(900L);
        assertThat(overview.getRelatedKeywordJob().getLatestFailCount()).isEqualTo(2);
        assertThat(overview.getRelatedKeywordJob().getLatestErrorCode()).isEqualTo("E001");

        assertThat(overview.getCategoryTrendJob().getLatestStatus()).isEqualTo("SUCCESS");
        assertThat(overview.getCategoryTrendJob().getLastSuccessAt()).isEqualTo(2300L);

        assertThat(overview.getShopSearchTrendJob().getLatestStatus()).isNull();
        assertThat(overview.getShopSearchTrendJob().getLastSuccessAt()).isNull();
        assertThat(overview.getShopSearchTrendJob().getLatestTotalCount()).isNull();
    }

    private CollectorJobExecution execution(
            String jobName,
            CollectorJobStatus status,
            long startedAt,
            long finishedAt,
            int totalCount,
            int successCount,
            int failCount,
            String errorCode,
            String errorMessage) {
        CollectorJobExecution execution = new CollectorJobExecution();
        execution.setJobName(jobName);
        execution.setStatus(status);
        execution.setTriggerType(CollectorTriggerType.SCHEDULED);
        execution.setStartedAt(startedAt);
        execution.setFinishedAt(finishedAt);
        execution.setDurationMs(finishedAt - startedAt);
        execution.setTotalCount(totalCount);
        execution.setSuccessCount(successCount);
        execution.setFailCount(failCount);
        execution.setFailedTargets(failCount > 0 ? List.of("target-1") : List.of());
        execution.setErrorCode(errorCode);
        execution.setErrorMessage(errorMessage);
        return execution;
    }
}
