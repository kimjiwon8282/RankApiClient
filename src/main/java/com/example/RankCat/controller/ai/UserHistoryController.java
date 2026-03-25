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
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. AI 분석 이력", description = "사용자별 AI 랭킹 예측 결과 저장 및 조회 API")
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

    @Operation(summary = "나의 분석 이력 조회", description = "최신순으로 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/histories")
    public ResponseEntity<Page<UserHistoryResponse.HistoryDto>> getHistories(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @ParameterObject // ✅ Swagger가 Pageable을 개별 파라미터(page, size, sort)로 풀어서 보여주게 합니다.
                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {

        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.getUserHistoriesWithPaging(loginUser, pageable));
    }

    @Operation(summary = "특정 상품의 기간별 랭킹 추이 조회", description = "파티셔닝 성능 테스트용 API입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/histories/product/{productId}")
    public ResponseEntity<List<UserHistoryResponse.HistoryDto>> getProductRankHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Parameter(description = "상품 ID", example = "123456789")
                    @PathVariable // ✅ 샘플 데이터에 맞게 수정
                    String productId,
            @Parameter(description = "시작일", example = "2026-03-01T00:00:00") // ✅ 2026년 3월로 수정
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @Parameter(description = "종료일", example = "2026-03-30T23:59:59") // ✅ 2026년 3월 30일로 고정
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate,
            @ParameterObject // ✅ Swagger에서 Pageable 파라미터가 개별 입력칸으로 풀리도록 추가
                    @PageableDefault(size = 100, sort = "createdAt", direction = Sort.Direction.ASC)
                    Pageable pageable) {
        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.getProductRankHistory(
                        loginUser, productId, startDate, endDate, pageable));
    }

    @Operation(summary = "특정 키워드 기준 분석 이력 검색", description = "복합 인덱스 성능 테스트용 API입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/histories/search")
    public ResponseEntity<Page<UserHistoryResponse.HistoryDto>> searchHistoriesByQuery(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Parameter(description = "검색 키워드", example = "가구바퀴") @RequestParam
                    String query, // ✅ 샘플 데이터에 맞게 수정
            @ParameterObject // ✅ Swagger에서 Pageable 파라미터가 개별 입력칸으로 풀리도록 추가
                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        User loginUser = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(
                userHistoryService.searchHistoriesByQuery(loginUser, query, pageable));
    }
}
