package com.example.RankCat.controller.user;

import com.example.RankCat.dto.AddUserRequest;
import com.example.RankCat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {
    private final UserService userService;
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
}
