package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.UserInfoResponse;
import com.example.RankCat.model.User; // 지완님의 실제 DB 엔티티
import com.example.RankCat.service.user.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "1. 사용자 정보", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
public class UserApiController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 이메일과 닉네임을 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        User user = userService.findByEmail(principal.getUsername());

        // 2) 이제 지완님의 User 엔티티이므로 닉네임과 이메일을 정상적으로 가져올 수 있습니다.
        return ResponseEntity.ok(new UserInfoResponse(user.getEmail(), user.getNickname()));
    }
}
