package com.atomicpay.transaction.domain;

public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}
