package com.example.RankCat.controller.api;

import com.example.RankCat.dto.api.CollectorJobExecutionDto;
import com.example.RankCat.dto.api.CollectorStatusOverviewDto;
import com.example.RankCat.service.api.interfaces.CollectorJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "7. 관리자 수집 상태 조회", description = "관리자가 최근 수집 실행 이력과 상태를 조회하는 읽기 전용 API")
@RestController
@RequestMapping("/admin/api/collectors")
@RequiredArgsConstructor
@Validated
public class AdminCollectorController {

    private final CollectorJobService collectorJobService;

    @Operation(summary = "최근 수집 실행 이력 조회", description = "최근 수집 잡 실행 이력을 최신순으로 조회합니다.")
    @GetMapping("/jobs")
    public ResponseEntity<List<CollectorJobExecutionDto>> getRecentJobs(
            @Parameter(description = "조회 개수", example = "20")
                    @RequestParam(defaultValue = "20")
                    @Min(1)
                    @Max(100)
                    int limit) {
        return ResponseEntity.ok(collectorJobService.getRecentExecutions(limit));
    }

    @Operation(summary = "잡별 최신 실행 이력 조회", description = "특정 잡의 가장 최근 실행 이력을 조회합니다.")
    @GetMapping("/jobs/{jobName}/latest")
    public ResponseEntity<CollectorJobExecutionDto> getLatestJob(
            @Parameter(description = "잡 이름", example = "rankcat.shop.search.trend")
                    @PathVariable
                    @NotBlank
                    String jobName) {
        return ResponseEntity.ok(collectorJobService.getLatestExecution(jobName));
    }

    @Operation(summary = "수집 상태 개요 조회", description = "핵심 수집 잡들의 최신 상태와 마지막 성공 시각을 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<CollectorStatusOverviewDto> getStatusOverview() {
        return ResponseEntity.ok(collectorJobService.getStatusOverview());
    }
}
