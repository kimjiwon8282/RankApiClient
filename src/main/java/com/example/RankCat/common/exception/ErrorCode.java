package com.example.RankCat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User (U)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),

    // Auth & Token (A)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A002", "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "A003", "인증 코드가 일치하지 않거나 만료되었습니다."),
    MISSING_AUTH_COOKIE(HttpStatus.UNAUTHORIZED, "A004", "필수 인증 쿠키가 누락되었습니다."),

    // Mail (M)
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M001", "이메일 발송에 실패했습니다."),

    // Common (C)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청하신 데이터를 찾을 수 없습니다."),

    // External API (E)
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "E001", "외부 API 연동 중 문제가 발생했습니다."),
    EXTERNAL_API_UNAUTHORIZED(HttpStatus.BAD_GATEWAY, "E002", "외부 API 인증에 실패했습니다."),
    EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "E003", "외부 API 응답이 지연되고 있습니다."),
    EXTERNAL_API_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "E004", "외부 API 요청 처리 중 문제가 발생했습니다."),
    EXTERNAL_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "E005", "외부 API 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
