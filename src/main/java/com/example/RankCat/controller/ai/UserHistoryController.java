package com.example.RankCat.controller.ai;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.UserRepository;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ai")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;
    private final UserRepository userRepository;

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody SaveHistoryRequest req) {

        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getUsername();            // 토큰의 sub(=email)
        User user = userRepository.findByEmail(email)      // 도메인 User 재조회
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        userHistoryService.save(user, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/histories")
    @ResponseBody
    public ResponseEntity<UserHistoryResponse> getHistories(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // principal.getUsername() == 이메일
        User loginUser = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        UserHistoryResponse response = userHistoryService.getUserHistories(loginUser);
        return ResponseEntity.ok(response);
    }
}
