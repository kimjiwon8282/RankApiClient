package com.example.RankCat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class AddUserRequest {
    @Schema(description = "사용자 이메일", example = "test@pusan.ac.kr")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email;

    @Schema(description = "사용자 닉네임", example = "랭캣개발자")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    String nickname;

    @Schema(description = "비밀번호 (8자 이상 권장)", example = "password123!")
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    String password;
}
