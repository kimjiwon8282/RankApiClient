package com.example.RankCat.controller.user;

import com.example.RankCat.dto.AddUserRequest;
import com.example.RankCat.service.user.EmailAuthService;
import com.example.RankCat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SignupController {
    private final UserService userService;
    private final EmailAuthService emailAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AddUserRequest addUserRequest) {
        try{
            userService.save(addUserRequest);
            return ResponseEntity.ok().body("회원가입이 완료되었습니다.");
        }catch (Exception e){
            return ResponseEntity.badRequest()
                    .body("회원가입에 실패했습니다"+e.getMessage());
        }
    }

    // 2. 이메일 중복 확인
    @GetMapping("/api/user/email-exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        log.info("이메일 존재 : "+exists);
        return ResponseEntity.ok().body(Map.of("exists", exists));
    }
    // 2. 인증코드 발송
    @PostMapping("/api/user/send-auth-code")
    public ResponseEntity<?> sendAuthCode(@RequestParam String email) {
        try {
            emailAuthService.sendAuthCode(email);
            return ResponseEntity.ok().body("인증 메일이 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증 메일 발송 실패: " + e.getMessage());
        }
    }

    // 3. 인증코드 검증
    @PostMapping("/api/user/verify-auth-code")
    public ResponseEntity<?> verifyAuthCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        boolean verified = emailAuthService.verifyAuthCode(email, code);
        if (verified) {
            return ResponseEntity.ok().body("인증 성공!");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 일치하지 않거나 만료되었습니다.");
        }
    }
}
