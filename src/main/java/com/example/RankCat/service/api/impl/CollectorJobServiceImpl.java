package com.example.RankCat.service.api.impl;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.config.schedule.CollectorJobNames;
import com.example.RankCat.dto.api.CollectorJobExecutionDto;
import com.example.RankCat.dto.api.CollectorJobStatusDto;
import com.example.RankCat.dto.api.CollectorStatusOverviewDto;
import com.example.RankCat.model.CollectorJobExecution;
import com.example.RankCat.model.CollectorJobStatus;
import com.example.RankCat.model.CollectorTriggerType;
import com.example.RankCat.repository.CollectorJobExecutionRepository;
import com.example.RankCat.service.api.interfaces.CollectorJobService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectorJobServiceImpl implements CollectorJobService {

    private final CollectorJobExecutionRepository collectorJobExecutionRepository;

    @Override
    public void recordExecution(
            String jobName,
            CollectorTriggerType triggerType,
            long startedAt,
            int totalCount,
            int successCount,
            List<String> failedTargets,
            String errorCode,
            String errorMessage) {
        long finishedAt = System.currentTimeMillis();
        List<String> safeFailedTargets =
                failedTargets != null ? new ArrayList<>(failedTargets) : List.of();
        int failCount = safeFailedTargets.size();

        CollectorJobExecution execution = new CollectorJobExecution();
        execution.setJobName(jobName);
        execution.setTriggerType(triggerType);
        execution.setStartedAt(startedAt);
        execution.setFinishedAt(finishedAt);
        execution.setDurationMs(Math.max(0, finishedAt - startedAt));
        execution.setTotalCount(totalCount);
        execution.setSuccessCount(successCount);
        execution.setFailCount(failCount);
        execution.setFailedTargets(safeFailedTargets);
        execution.setErrorCode(errorCode);
        execution.setErrorMessage(errorMessage);
        execution.setStatus(resolveStatus(totalCount, successCount, failCount));

        collectorJobExecutionRepository.save(execution);
    }

    @Override
    public List<CollectorJobExecutionDto> getRecentExecutions(int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 100);
        return collectorJobExecutionRepository
                .findAllByOrderByStartedAtDesc(PageRequest.of(0, normalizedLimit))
                .stream()
                .map(CollectorJobExecutionDto::fromEntity)
                .toList();
    }

    @Override
    public CollectorJobExecutionDto getLatestExecution(String jobName) {
        CollectorJobExecution latest =
                collectorJobExecutionRepository
                        .findFirstByJobNameOrderByStartedAtDesc(jobName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
        return CollectorJobExecutionDto.fromEntity(latest);
    }

    @Override
    public CollectorStatusOverviewDto getStatusOverview() {
        return CollectorStatusOverviewDto.builder()
                .checkedAt(System.currentTimeMillis())
                .relatedKeywordJob(buildJobStatus(CollectorJobNames.RELATED_KEYWORD_COLLECT))
                .categoryTrendJob(buildJobStatus(CollectorJobNames.CATEGORY_TREND_COLLECT))
                .shopSearchTrendJob(buildJobStatus(CollectorJobNames.SHOP_SEARCH_TREND_COLLECT))
                .build();
    }

    private CollectorJobStatus resolveStatus(int totalCount, int successCount, int failCount) {
        if (failCount == 0) {
            return CollectorJobStatus.SUCCESS;
        }
        if (successCount == 0 || totalCount == 0) {
            return CollectorJobStatus.FAIL;
        }
        return CollectorJobStatus.PARTIAL_SUCCESS;
    }

    private CollectorJobStatusDto buildJobStatus(String jobName) {
        Optional<CollectorJobExecution> latestOpt =
                collectorJobExecutionRepository.findFirstByJobNameOrderByStartedAtDesc(jobName);
        Optional<CollectorJobExecution> latestSuccessOpt =
                collectorJobExecutionRepository.findFirstByJobNameAndStatusOrderByFinishedAtDesc(
                        jobName, CollectorJobStatus.SUCCESS);

        return CollectorJobStatusDto.builder()
                .jobName(jobName)
                .latestStatus(latestOpt.map(e -> e.getStatus().name()).orElse(null))
                .latestStartedAt(latestOpt.map(CollectorJobExecution::getStartedAt).orElse(null))
                .latestFinishedAt(latestOpt.map(CollectorJobExecution::getFinishedAt).orElse(null))
                .lastSuccessAt(
                        latestSuccessOpt.map(CollectorJobExecution::getFinishedAt).orElse(null))
                .latestTotalCount(latestOpt.map(CollectorJobExecution::getTotalCount).orElse(null))
                .latestSuccessCount(
                        latestOpt.map(CollectorJobExecution::getSuccessCount).orElse(null))
                .latestFailCount(latestOpt.map(CollectorJobExecution::getFailCount).orElse(null))
                .latestErrorCode(latestOpt.map(CollectorJobExecution::getErrorCode).orElse(null))
                .latestErrorMessage(
                        latestOpt.map(CollectorJobExecution::getErrorMessage).orElse(null))
                .build();
    }
}
