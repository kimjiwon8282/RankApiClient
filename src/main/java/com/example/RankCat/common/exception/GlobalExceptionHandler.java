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
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn(
                "BusinessException: code={}, message={}",
                errorCode.getCode(),
                e.getResolvedMessage());
        ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException e) {
        log.warn("Login Failure: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND);
        return new ResponseEntity<>(response, ErrorCode.USER_NOT_FOUND.getStatus());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    protected ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException e) {
        log.warn("Login Failure (User Not Found): {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND);
        return new ResponseEntity<>(response, ErrorCode.USER_NOT_FOUND.getStatus());
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
            MissingRequestCookieException e) {
        log.warn("Missing Request Cookie: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.MISSING_AUTH_COOKIE);
        return new ResponseEntity<>(response, ErrorCode.MISSING_AUTH_COOKIE.getStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing Request Parameter: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.warn("Constraint Violation: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }
}
