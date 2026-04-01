package com.example.RankCat.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        this(errorCode, detailMessage, null);
    }

    public BusinessException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(
                detailMessage != null && !detailMessage.isBlank()
                        ? detailMessage
                        : errorCode.getMessage(),
                cause);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public String getResolvedMessage() {
        return detailMessage != null && !detailMessage.isBlank()
                ? detailMessage
                : errorCode.getMessage();
    }
}
