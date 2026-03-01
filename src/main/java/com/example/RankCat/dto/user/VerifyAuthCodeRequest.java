package com.example.RankCat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "이메일 인증코드 검증 요청 DTO")
public class VerifyAuthCodeRequest {

    @Schema(description = "인증을 진행할 이메일", example = "test@pusan.ac.kr")
    private String email;

    @Schema(description = "메일로 발송된 6자리 인증코드", example = "123456")
    private String code;
}
