package com.example.RankCat.dto.api;

import com.example.RankCat.model.CollectorJobExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "수집 잡 실행 이력 응답")
public class CollectorJobExecutionDto {

    @Schema(description = "잡 이름", example = "rankcat.shop.search.trend")
    private final String jobName;

    @Schema(description = "실행 상태", example = "PARTIAL_SUCCESS")
    private final String status;

    @Schema(description = "실행 방식", example = "SCHEDULED")
    private final String triggerType;

    @Schema(description = "시작 시각 (Unix Timestamp)", example = "1711977600000")
    private final long startedAt;

    @Schema(description = "종료 시각 (Unix Timestamp)", example = "1711977900000")
    private final long finishedAt;

    @Schema(description = "총 실행 시간(ms)", example = "300000")
    private final long durationMs;

    @Schema(description = "전체 처리 대상 수", example = "120")
    private final int totalCount;

    @Schema(description = "성공 건수", example = "118")
    private final int successCount;

    @Schema(description = "실패 건수", example = "2")
    private final int failCount;

    @Schema(description = "실패한 대상 목록")
    private final List<String> failedTargets;

    @Schema(description = "마지막 오류 코드", example = "E003")
    private final String errorCode;

    @Schema(description = "마지막 오류 메시지")
    private final String errorMessage;

    @Builder
    public CollectorJobExecutionDto(
            String jobName,
            String status,
            String triggerType,
            long startedAt,
            long finishedAt,
            long durationMs,
            int totalCount,
            int successCount,
            int failCount,
            List<String> failedTargets,
            String errorCode,
            String errorMessage) {
        this.jobName = jobName;
        this.status = status;
        this.triggerType = triggerType;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMs = durationMs;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.failedTargets = failedTargets;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static CollectorJobExecutionDto fromEntity(CollectorJobExecution entity) {
        if (entity == null) {
            return null;
        }
        return CollectorJobExecutionDto.builder()
                .jobName(entity.getJobName())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .triggerType(
                        entity.getTriggerType() != null ? entity.getTriggerType().name() : null)
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .durationMs(entity.getDurationMs())
                .totalCount(entity.getTotalCount())
                .successCount(entity.getSuccessCount())
                .failCount(entity.getFailCount())
                .failedTargets(entity.getFailedTargets())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .build();
    }
}
