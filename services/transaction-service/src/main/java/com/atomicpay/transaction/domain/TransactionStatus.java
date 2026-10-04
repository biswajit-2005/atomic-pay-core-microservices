package com.atomicpay.transaction.domain;

public enum TransactionStatus {
    INITIATED,
    VALIDATING,
    PROCESSING,
    DEBITED,
    CREDITED,
    COMPLETED,
    PENDING,
    COMPENSATING,
    COMPENSATED,
    FAILED,
    RECONCILIATION_REQUIRED
}
