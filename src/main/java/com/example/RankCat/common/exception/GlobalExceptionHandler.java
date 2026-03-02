package com.example.RankCat.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 우리가 만든 BusinessException이 터지면 여기서 잡아서 ErrorResponse로 변환
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // 로그인 인증 실패 처리
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException e) {
        log.warn("Login Failure: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND); // U001
        return new ResponseEntity<>(response, ErrorCode.USER_NOT_FOUND.getStatus());
    }

    // DB에 없는 사용자 이메일로 로그인 시도 시 발생하는 예외 처리
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    protected ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException e) {
        log.warn("Login Failure (User Not Found): {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND); // U001
        return new ResponseEntity<>(response, ErrorCode.USER_NOT_FOUND.getStatus());
    }

    // 필수 쿠키 누락 처리 (500 에러 방어)
    @ExceptionHandler(MissingRequestCookieException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
            MissingRequestCookieException e) {
        log.warn("Missing Request Cookie: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.MISSING_AUTH_COOKIE);
        return new ResponseEntity<>(response, ErrorCode.MISSING_AUTH_COOKIE.getStatus());
    }

    // 예상치 못한 그 외의 모든 런타임 에러(NullPointerException 등)를 잡는 Fallback
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e); // 여기서는 에러 로그를 길게 남겨서 버그 추적
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // @RequestParam 필수 파라미터가 아예 누락되었을 때 (null)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing Request Parameter: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // @Validated로 검증한 단일 파라미터(@RequestParam)가 조건(@NotBlank 등)에 맞지 않을 때
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.warn("Constraint Violation: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }
}
