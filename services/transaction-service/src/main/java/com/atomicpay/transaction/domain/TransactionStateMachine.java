package com.atomicpay.transaction.domain;

import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;

import java.util.*;

public class TransactionStateMachine {

    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TransactionStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TransactionStatus.INITIATED, Set.of(TransactionStatus.VALIDATING));

        ALLOWED_TRANSITIONS.put(TransactionStatus.VALIDATING, Set.of(TransactionStatus.PROCESSING, TransactionStatus.FAILED));

        ALLOWED_TRANSITIONS.put(TransactionStatus.PROCESSING, Set.of(TransactionStatus.DEBITED, TransactionStatus.PENDING, TransactionStatus.FAILED));

        ALLOWED_TRANSITIONS.put(TransactionStatus.DEBITED, Set.of(TransactionStatus.CREDITED, TransactionStatus.COMPENSATING, TransactionStatus.PENDING));

        ALLOWED_TRANSITIONS.put(TransactionStatus.CREDITED, Set.of(TransactionStatus.COMPLETED, TransactionStatus.PENDING));

        ALLOWED_TRANSITIONS.put(TransactionStatus.COMPENSATING, Set.of(TransactionStatus.COMPENSATED, TransactionStatus.PENDING));

        ALLOWED_TRANSITIONS.put(TransactionStatus.PENDING, Set.of(TransactionStatus.COMPLETED, TransactionStatus.FAILED, TransactionStatus.RECONCILIATION_REQUIRED));

        ALLOWED_TRANSITIONS.put(TransactionStatus.RECONCILIATION_REQUIRED, Set.of(TransactionStatus.COMPLETED, TransactionStatus.COMPENSATED, TransactionStatus.FAILED));

        // Terminal states
        ALLOWED_TRANSITIONS.put(TransactionStatus.COMPLETED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(TransactionStatus.COMPENSATED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(TransactionStatus.FAILED, Collections.emptySet());
    }

    public static boolean isValidTransition(TransactionStatus from, TransactionStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Set<TransactionStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static void validateTransition(TransactionStatus current, TransactionStatus next) {
        if (!isValidTransition(current, next)) {
            throw new TransactionException(
                    ErrorCode.INVALID_TRANSACTION_STATE,
                    String.format("Invalid transaction state transition from %s to %s", current, next)
            );
        }
    }
}
