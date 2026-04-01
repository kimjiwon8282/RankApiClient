package com.example.RankCat.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "잡별 최신 실행 상태 요약")
public class CollectorJobStatusDto {

    @Schema(description = "잡 이름", example = "rankcat.keyword.collect")
    private final String jobName;

    @Schema(description = "최신 실행 상태", example = "SUCCESS")
    private final String latestStatus;

    @Schema(description = "최신 실행 시작 시각 (Unix Timestamp)", example = "1711977600000")
    private final Long latestStartedAt;

    @Schema(description = "최신 실행 종료 시각 (Unix Timestamp)", example = "1711977900000")
    private final Long latestFinishedAt;

    @Schema(description = "가장 최근 성공 시각 (Unix Timestamp)", example = "1711977900000")
    private final Long lastSuccessAt;

    @Schema(description = "최신 전체 대상 수", example = "120")
    private final Integer latestTotalCount;

    @Schema(description = "최신 성공 건수", example = "118")
    private final Integer latestSuccessCount;

    @Schema(description = "최신 실패 건수", example = "2")
    private final Integer latestFailCount;

    @Schema(description = "최신 오류 코드", example = "E003")
    private final String latestErrorCode;

    @Schema(description = "최신 오류 메시지")
    private final String latestErrorMessage;

    @Builder
    public CollectorJobStatusDto(
            String jobName,
            String latestStatus,
            Long latestStartedAt,
            Long latestFinishedAt,
            Long lastSuccessAt,
            Integer latestTotalCount,
            Integer latestSuccessCount,
            Integer latestFailCount,
            String latestErrorCode,
            String latestErrorMessage) {
        this.jobName = jobName;
        this.latestStatus = latestStatus;
        this.latestStartedAt = latestStartedAt;
        this.latestFinishedAt = latestFinishedAt;
        this.lastSuccessAt = lastSuccessAt;
        this.latestTotalCount = latestTotalCount;
        this.latestSuccessCount = latestSuccessCount;
        this.latestFailCount = latestFailCount;
        this.latestErrorCode = latestErrorCode;
        this.latestErrorMessage = latestErrorMessage;
    }
}
