package com.atomicpay.transaction.domain;

import com.atomicpay.transaction.exception.TransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStateMachineTest {

    @Test
    @DisplayName("Should allow valid forward transaction state transitions")
    void validTransitions() {
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.INITIATED, TransactionStatus.VALIDATING));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.VALIDATING, TransactionStatus.PROCESSING));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.PROCESSING, TransactionStatus.DEBITED));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.DEBITED, TransactionStatus.CREDITED));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.CREDITED, TransactionStatus.COMPLETED));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.DEBITED, TransactionStatus.COMPENSATING));
        assertTrue(TransactionStateMachine.isValidTransition(TransactionStatus.COMPENSATING, TransactionStatus.COMPENSATED));
    }

    @Test
    @DisplayName("Should disallow invalid state transitions and throw TransactionException")
    void invalidTransitions_ThrowsException() {
        assertFalse(TransactionStateMachine.isValidTransition(TransactionStatus.INITIATED, TransactionStatus.COMPLETED));
        assertFalse(TransactionStateMachine.isValidTransition(TransactionStatus.COMPLETED, TransactionStatus.FAILED));
        assertFalse(TransactionStateMachine.isValidTransition(TransactionStatus.FAILED, TransactionStatus.PROCESSING));

        assertThrows(TransactionException.class, () ->
                TransactionStateMachine.validateTransition(TransactionStatus.INITIATED, TransactionStatus.COMPLETED));
    }
}
