package com.atomicpay.transaction.exception;

import java.util.Collections;
import java.util.Map;

public class TransactionException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public TransactionException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    public TransactionException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    public TransactionException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public Map<String, Object> getDetails() { return details; }
}
