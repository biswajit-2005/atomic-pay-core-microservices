package com.atomicpay.auth.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request format or parameters"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication credentials missing or invalid"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Insufficient permissions to perform this action"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User with this email already exists"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT or refresh token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token signature or structure is invalid"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "User account is suspended"),
    USER_LOCKED(HttpStatus.FORBIDDEN, "User account is locked"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
