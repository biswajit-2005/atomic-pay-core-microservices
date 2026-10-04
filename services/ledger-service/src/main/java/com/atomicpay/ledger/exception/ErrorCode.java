package com.atomicpay.ledger.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden access"),
    LEDGER_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Ledger account not found"),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found in ledger"),
    UNBALANCED_LEDGER_ENTRIES(HttpStatus.BAD_REQUEST, "Ledger entries are unbalanced (total debit != total credit)"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Ledger entry amount must be positive"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal ledger error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getDefaultMessage() { return defaultMessage; }
}
