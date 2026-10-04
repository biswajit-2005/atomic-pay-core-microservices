package com.atomicpay.transaction.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid transfer request parameters"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized transfer request"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden operation"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "Account is frozen or inactive"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient balance"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Transfer amount must be greater than zero"),
    CURRENCY_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Currency not supported (INR required)"),
    SELF_TRANSFER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Sender and receiver accounts must be different"),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required"),
    IDEMPOTENCY_KEY_REUSED(HttpStatus.CONFLICT, "Idempotency key has already been used for a different request payload"),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
    TRANSACTION_PENDING(HttpStatus.ACCEPTED, "Transaction status is pending"),
    CONCURRENT_OPERATION(HttpStatus.CONFLICT, "Concurrent transaction operation in progress"),
    INVALID_TRANSACTION_STATE(HttpStatus.CONFLICT, "Invalid transaction state transition"),
    SAGA_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction Saga execution failed"),
    RECONCILIATION_REQUIRED(HttpStatus.ACCEPTED, "Transaction requires manual or automated reconciliation"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getDefaultMessage() { return defaultMessage; }
}
