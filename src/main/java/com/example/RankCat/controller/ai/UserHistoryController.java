package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import com.example.RankCat.service.user.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. AI 분석 이력", description = "사용자별 AI 랭킹 예측 결과 저장 및 조회 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;
    private final UserService userService;

    @Operation(
            summary = "AI 예측 결과 저장",
            description = "AI 모델이 예측한 특정 상품의 랭킹 결과를 사용자의 분석 히스토리에 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    @PostMapping("/save")
    public ResponseEntity<?> saveHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody SaveHistoryRequest req) {
        User user = userService.findByEmail(principal.getUsername());

        userHistoryService.save(user, req);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "나의 분석 이력 조회",
            description = "createdAt, id 복합 커서를 기준으로 최신순 키셋 페이지네이션 조회를 수행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "400", description = "잘못된 커서 또는 size 값", content = @Content)
    })
    @GetMapping("/histories")
    public ResponseEntity<UserHistoryResponse.HistorySliceResponse> getHistories(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Parameter(
                            description = "이전 응답의 nextCursorCreatedAt 값",
                            example = "2026-03-30T12:34:56")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime cursorCreatedAt,
            @Parameter(description = "이전 응답의 nextCursorId 값", example = "120")
                    @RequestParam(required = false)
                    Long cursorId,
            @Parameter(description = "조회 개수", example = "20")
                    @RequestParam(defaultValue = "20")
                    @Min(1)
                    @Max(100)
                    int size) {

        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.getUserHistoriesWithCursor(
                        loginUser, cursorCreatedAt, cursorId, size));
    }

    @Operation(summary = "특정 상품의 기간별 랭킹 추이 조회", description = "파티셔닝 성능 테스트용 API입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/histories/product/{productId}")
    public ResponseEntity<List<UserHistoryResponse.HistoryDto>> getProductRankHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Parameter(description = "상품 ID", example = "123456789") @PathVariable String productId,
            @Parameter(description = "시작일", example = "2026-03-01T00:00:00")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @Parameter(description = "종료일", example = "2026-03-30T23:59:59")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate,
            @ParameterObject
                    @PageableDefault(size = 100, sort = "createdAt", direction = Sort.Direction.ASC)
                    Pageable pageable) {
        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.getProductRankHistory(
                        loginUser, productId, startDate, endDate, pageable));
    }

    @Operation(
            summary = "특정 키워드 기준 분석 이력 검색",
            description = "query + createdAt, id 복합 커서를 기준으로 최신순 키셋 페이지네이션 조회를 수행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "400", description = "잘못된 커서 또는 size 값", content = @Content)
    })
    @GetMapping("/histories/search")
    public ResponseEntity<UserHistoryResponse.HistorySliceResponse> searchHistoriesByQuery(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Parameter(description = "검색 키워드", example = "가구바퀴") @RequestParam String query,
            @Parameter(
                            description = "이전 응답의 nextCursorCreatedAt 값",
                            example = "2026-03-30T12:34:56")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime cursorCreatedAt,
            @Parameter(description = "이전 응답의 nextCursorId 값", example = "120")
                    @RequestParam(required = false)
                    Long cursorId,
            @Parameter(description = "조회 개수", example = "20")
                    @RequestParam(defaultValue = "20")
                    @Min(1)
                    @Max(100)
                    int size) {
        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.searchHistoriesByQuery(
                        loginUser, query, cursorCreatedAt, cursorId, size));
    }
}
