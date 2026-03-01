package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.AddUserRequest;
import com.example.RankCat.dto.user.VerifyAuthCodeRequest;
import com.example.RankCat.service.user.impl.UserService;
import com.example.RankCat.service.user.interfaces.EmailAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. 회원가입 및 인증", description = "사용자 등록 및 이메일 인증 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SignupController {
    private final UserService userService;
    private final EmailAuthService emailAuthService;

    @Operation(summary = "일반 회원가입", description = "이메일, 비밀번호, 닉네임을 사용하여 새로운 사용자를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (검증 실패 또는 중복 이메일)",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = {
                                    @ExampleObject(
                                            name = "C001",
                                            summary = "입력값 검증 실패",
                                            value =
                                                    "{\"status\":400,\"code\":\"C001\",\"message\":\"입력값이 올바르지 않습니다.\"}"),
                                    @ExampleObject(
                                            name = "U002",
                                            summary = "이메일 중복",
                                            value =
                                                    "{\"status\":400,\"code\":\"U002\",\"message\":\"이미 존재하는 이메일입니다.\"}")
                                }))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddUserRequest addUserRequest) {
        userService.save(addUserRequest);
        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");
    }

    // 2. 이메일 중복 확인
    @Operation(summary = "이메일 중복 확인", description = "입력한 이메일이 이미 가입되어 있는지 실시간으로 확인합니다.")
    @GetMapping("/api/user/email-exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        log.info("이메일 존재 : " + exists);
        return ResponseEntity.ok().body(Map.of("exists", exists));
    }

    // 2. 인증코드 발송
    @Operation(summary = "이메일 인증코드 발송", description = "입력한 이메일로 6자리 숫자의 인증코드를 발송합니다. (5분간 유효)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메일 발송 성공"),
        @ApiResponse(responseCode = "500", description = "(M001) 메일 발송 시스템 오류")
    })
    @PostMapping("/api/user/send-auth-code")
    public ResponseEntity<?> sendAuthCode(@RequestParam String email) {
        // 예외가 발생하면 EmailAuthService나 MailService에서
        // BusinessException을 던지고 GlobalExceptionHandler가 알아서 처리합니다.
        emailAuthService.sendAuthCode(email);
        return ResponseEntity.ok().body("인증 메일이 발송되었습니다.");
    }

    // 3. 인증코드 검증
    @Operation(summary = "인증코드 검증", description = "사용자가 입력한 인증코드가 발송된 코드와 일치하는지 검증합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공"),
        @ApiResponse(responseCode = "400", description = "(A003) 인증번호 불일치 혹은 만료")
    })
    @PostMapping("/api/user/verify-auth-code")
    public ResponseEntity<?> verifyAuthCode(
            @RequestBody VerifyAuthCodeRequest request) { // [수정] Map -> DTO
        emailAuthService.verifyAuthCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok().body("인증 성공!");
    }
}
