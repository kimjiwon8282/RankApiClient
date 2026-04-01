package com.example.RankCat.service.api.interfaces;

import com.example.RankCat.dto.api.CollectorJobExecutionDto;
import com.example.RankCat.dto.api.CollectorStatusOverviewDto;
import com.example.RankCat.model.CollectorTriggerType;
import java.util.List;

public interface CollectorJobService {
    void recordExecution(
            String jobName,
            CollectorTriggerType triggerType,
            long startedAt,
            int totalCount,
            int successCount,
            List<String> failedTargets,
            String errorCode,
            String errorMessage);

    List<CollectorJobExecutionDto> getRecentExecutions(int limit);

    CollectorJobExecutionDto getLatestExecution(String jobName);

    CollectorStatusOverviewDto getStatusOverview();
}
