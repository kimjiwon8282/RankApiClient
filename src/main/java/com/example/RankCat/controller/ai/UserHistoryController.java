package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import com.example.RankCat.service.user.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. AI 분석 이력", description = "사용자별 AI 랭킹 예측 결과 저장 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;
    private final UserService userService; // ✅ UserRepository 대신 주입

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
            description = "현재 로그인한 사용자가 과거에 분석했던 전체 상품 리스트와 예측 결과를 최신순으로 조회합니다. **(인증 필수)**")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/histories")
    public ResponseEntity<UserHistoryResponse> getHistories(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User loginUser = userService.findByEmail(principal.getUsername());

        UserHistoryResponse response = userHistoryService.getUserHistories(loginUser);
        return ResponseEntity.ok(response);
    }
}
