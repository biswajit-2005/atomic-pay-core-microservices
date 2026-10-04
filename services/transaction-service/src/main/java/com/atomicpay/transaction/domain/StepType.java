package com.atomicpay.transaction.domain;

public enum StepType {
    VALIDATE,
    DEBIT,
    CREDIT,
    LEDGER,
    COMPLETE,
    COMPENSATE_DEBIT
}
