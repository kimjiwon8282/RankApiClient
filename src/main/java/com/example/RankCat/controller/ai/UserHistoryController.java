package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import com.example.RankCat.service.user.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController // ✅ @Controller + @ResponseBody를 하나로 합침
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;
    private final UserService userService; // ✅ UserRepository 대신 주입

    @PostMapping("/save")
    public ResponseEntity<?> saveHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody SaveHistoryRequest req) {
        User user = userService.findByEmail(principal.getUsername());

        userHistoryService.save(user, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/histories")
    public ResponseEntity<UserHistoryResponse> getHistories(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User loginUser = userService.findByEmail(principal.getUsername());

        UserHistoryResponse response = userHistoryService.getUserHistories(loginUser);
        return ResponseEntity.ok(response);
    }
}
