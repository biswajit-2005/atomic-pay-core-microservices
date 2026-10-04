package com.atomicpay.transaction.service;

import com.atomicpay.transaction.client.AccountBalanceService;
import com.atomicpay.transaction.domain.TransactionStatus;
import com.atomicpay.transaction.dto.*;
import com.atomicpay.transaction.entity.IdempotencyRecordEntity;
import com.atomicpay.transaction.entity.TransactionEntity;
import com.atomicpay.transaction.entity.TransactionStepEntity;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.repository.TransactionRepository;
import com.atomicpay.transaction.repository.TransactionStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionStepRepository transactionStepRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private AccountBalanceService accountBalanceService;

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private jakarta.persistence.Query query;

    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private TransferService transferService;

    private UUID userId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private String idempotencyKey;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        senderAccountId = UUID.randomUUID();
        receiverAccountId = UUID.randomUUID();
        idempotencyKey = "test-idempotency-key-123";

        transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("1000.0000"))
                .currency("INR")
                .build();

        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.executeUpdate()).thenReturn(1);
    }

    @Test
    @DisplayName("Should successfully execute transfer with idempotency record and completed steps")
    void processTransfer_Success() {
        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("1000.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash_123");
        when(idempotencyService.checkExistingRecord(eq(userId), eq(idempotencyKey), anyString())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(eq(userId), eq(idempotencyKey), anyString())).thenReturn(idempotencyRecord);
        when(transactionRepository.findBySenderAccountIdAndIdempotencyKey(senderAccountId, idempotencyKey)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transferService.processTransfer(idempotencyKey, userId, transferRequest, "correlation-id-123");

        assertNotNull(response);
        assertEquals(senderAccountId, response.getSenderAccountId());
        assertEquals(receiverAccountId, response.getReceiverAccountId());
        assertEquals(new BigDecimal("1000.0000"), response.getAmount());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());

        verify(accountBalanceService, times(1)).executeDebit(senderAccountId, new BigDecimal("1000.0000"));
        verify(accountBalanceService, times(1)).executeCredit(receiverAccountId, new BigDecimal("1000.0000"));
        verify(idempotencyService, times(1)).markCompleted(eq(idempotencyRecord), any(), eq(200), eq("COMPLETED"));
    }

    @Test
    @DisplayName("Should trigger Saga compensation when credit step fails")
    void processTransfer_CreditFailure_TriggersSagaCompensation() {
        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("1000.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder().id(UUID.randomUUID()).build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash");
        when(idempotencyService.checkExistingRecord(any(), any(), any())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(any(), any(), any())).thenReturn(idempotencyRecord);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionException ex = assertThrows(TransactionException.class, () ->
                transferService.processTransfer(idempotencyKey, userId, transferRequest, "cid", "FAIL_CREDIT"));

        assertEquals(ErrorCode.SAGA_FAILED, ex.getErrorCode());
        verify(accountBalanceService, times(1)).executeDebit(senderAccountId, new BigDecimal("1000.0000"));
        verify(accountBalanceService, times(1)).executeCompensationDebit(senderAccountId, new BigDecimal("1000.0000"));
        verify(idempotencyService, times(1)).markCompleted(eq(idempotencyRecord), any(), eq(422), contains("COMPENSATED"));
    }

    @Test
    @DisplayName("Should mark transaction as RECONCILIATION_REQUIRED on credit timeout")
    void processTransfer_CreditTimeout_TransitionsToReconciliationRequired() {
        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("1000.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder().id(UUID.randomUUID()).build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash");
        when(idempotencyService.checkExistingRecord(any(), any(), any())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(any(), any(), any())).thenReturn(idempotencyRecord);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionException ex = assertThrows(TransactionException.class, () ->
                transferService.processTransfer(idempotencyKey, userId, transferRequest, "cid", "DELAY_CREDIT"));

        assertEquals(ErrorCode.TRANSACTION_PENDING, ex.getErrorCode());
        verify(accountBalanceService, times(1)).executeDebit(senderAccountId, new BigDecimal("1000.0000"));
        verify(idempotencyService, times(1)).markCompleted(eq(idempotencyRecord), any(), eq(504), contains("RECONCILIATION_REQUIRED"));
    }

    @Test
    @DisplayName("Should throw IDEMPOTENCY_KEY_REQUIRED when idempotency key is missing")
    void processTransfer_MissingIdempotencyKey_ThrowsException() {
        TransactionException ex = assertThrows(TransactionException.class, () ->
                transferService.processTransfer(null, userId, transferRequest, "cid"));

        assertEquals(ErrorCode.IDEMPOTENCY_KEY_REQUIRED, ex.getErrorCode());
        verify(accountBalanceService, never()).executeTransfer(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw SELF_TRANSFER_NOT_ALLOWED when sender and receiver account IDs are equal")
    void processTransfer_SelfTransfer_ThrowsException() {
        TransferRequest selfRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(senderAccountId)
                .amount(new BigDecimal("500.0000"))
                .currency("INR")
                .build();

        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(senderAccountId)
                .amount(new BigDecimal("500.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder().id(UUID.randomUUID()).build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash");
        when(idempotencyService.checkExistingRecord(any(), any(), any())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(any(), any(), any())).thenReturn(idempotencyRecord);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionException ex = assertThrows(TransactionException.class, () ->
                transferService.processTransfer(idempotencyKey, userId, selfRequest, "cid"));

        assertEquals(ErrorCode.SELF_TRANSFER_NOT_ALLOWED, ex.getErrorCode());
    }
}
