package com.example.RankCat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    @Schema(description = "로그인용 이메일", example = "test@pusan.ac.kr")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    private String password;
}
