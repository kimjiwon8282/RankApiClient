package com.example.RankCat.controller.user;


import com.example.RankCat.config.jwt.TokenProvider;
import com.example.RankCat.dto.UserInfoResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {

    private final TokenProvider tokenProvider;  // JWT 유효성 검사 및 클레임 추출
    private final UserService userService;      // User 조회 서비스

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // 1) "Bearer <token>" 형태에서 실제 토큰 부분만 추출
        String token = authorizationHeader.replaceFirst("^Bearer\\s+", "");
        // 2) 토큰에서 사용자 ID(claim "id")를 꺼냄
        Long userId = tokenProvider.getUserId(token);
        // 3) DB에서 User 엔티티 조회
        User user = userService.findById(userId);

        // 4) 이메일·닉네임을 담아 응답
        UserInfoResponse response = new UserInfoResponse(
                user.getEmail(),
                user.getNickname()
        );
        return ResponseEntity.ok(response);
    }
}
