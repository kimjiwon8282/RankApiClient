package com.example.RankCat.controller.user;

import com.example.RankCat.dto.user.AddUserRequest;
import com.example.RankCat.service.user.impl.UserService;
import com.example.RankCat.service.user.interfaces.EmailAuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SignupController {
    private final UserService userService;
    private final EmailAuthService emailAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddUserRequest addUserRequest) {
        userService.save(addUserRequest);
        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");
    }

    // 2. 이메일 중복 확인
    @GetMapping("/api/user/email-exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        log.info("이메일 존재 : " + exists);
        return ResponseEntity.ok().body(Map.of("exists", exists));
    }

    // 2. 인증코드 발송
    @PostMapping("/api/user/send-auth-code")
    public ResponseEntity<?> sendAuthCode(@RequestParam String email) {
        // 예외가 발생하면 EmailAuthService나 MailService에서
        // BusinessException을 던지고 GlobalExceptionHandler가 알아서 처리합니다.
        emailAuthService.sendAuthCode(email);
        return ResponseEntity.ok().body("인증 메일이 발송되었습니다.");
    }

    // 3. 인증코드 검증
    @PostMapping("/api/user/verify-auth-code")
    public ResponseEntity<?> verifyAuthCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        emailAuthService.verifyAuthCode(email, code);
        return ResponseEntity.ok().body("인증 성공!");
    }
}
