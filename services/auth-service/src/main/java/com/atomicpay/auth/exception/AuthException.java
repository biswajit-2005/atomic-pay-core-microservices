package com.atomicpay.auth.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    public AuthException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
