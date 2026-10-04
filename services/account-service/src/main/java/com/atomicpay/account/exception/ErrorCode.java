package com.atomicpay.account.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access forbidden"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "Account is frozen or closed"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient account balance"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid monetary amount"),
    CURRENCY_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Currency is not supported (INR required for MVP)"),
    BENEFICIARY_ALREADY_EXISTS(HttpStatus.CONFLICT, "Beneficiary already added"),
    CONCURRENT_OPERATION(HttpStatus.CONFLICT, "Concurrent transaction in progress"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getDefaultMessage() { return defaultMessage; }
}
