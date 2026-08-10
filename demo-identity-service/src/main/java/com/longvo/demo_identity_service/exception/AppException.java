package com.longvo.demo_identity_service.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private ErrorCode errorCode;

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
