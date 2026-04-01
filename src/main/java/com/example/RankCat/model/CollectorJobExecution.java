package com.example.RankCat.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "collector_job_executions")
public class CollectorJobExecution {
    @Id private String id;

    private String jobName;
    private CollectorJobStatus status;
    private CollectorTriggerType triggerType;

    private long startedAt;
    private long finishedAt;
    private long durationMs;

    private int totalCount;
    private int successCount;
    private int failCount;

    private List<String> failedTargets = new ArrayList<>();

    private String errorCode;
    private String errorMessage;
}
