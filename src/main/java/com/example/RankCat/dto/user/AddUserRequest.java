package com.example.RankCat.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    String nickname;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    String password;
}
