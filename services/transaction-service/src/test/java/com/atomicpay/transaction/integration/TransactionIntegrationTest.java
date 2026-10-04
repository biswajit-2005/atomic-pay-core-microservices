package com.atomicpay.transaction.integration;

import com.atomicpay.transaction.client.AccountBalanceService;
import com.atomicpay.transaction.domain.IdempotencyStatus;
import com.atomicpay.transaction.domain.TransactionStatus;
import com.atomicpay.transaction.dto.TransferRequest;
import com.atomicpay.transaction.dto.TransactionResponse;
import com.atomicpay.transaction.entity.IdempotencyRecordEntity;
import com.atomicpay.transaction.entity.TransactionEntity;
import com.atomicpay.transaction.entity.TransactionStepEntity;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.repository.TransactionRepository;
import com.atomicpay.transaction.repository.TransactionStepRepository;
import com.atomicpay.transaction.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionIntegrationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionStepRepository transactionStepRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private AccountBalanceService accountBalanceService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private OutboxPublisherService outboxPublisherService;

    @Mock
    private TransactionMetricsService metricsService;

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
        idempotencyKey = "integration-idempotency-key-777";

        transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("2500.0000"))
                .currency("INR")
                .build();

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
    }

    @Test
    @DisplayName("Integration Test 1: Complete Happy Path Financial Transfer Workflow")
    void testHappyPathTransferWorkflow() {
        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("2500.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .requestHash("payload_hash_777")
                .status(IdempotencyStatus.PROCESSING)
                .build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash_777");
        when(idempotencyService.checkExistingRecord(eq(userId), eq(idempotencyKey), anyString())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(eq(userId), eq(idempotencyKey), anyString())).thenReturn(idempotencyRecord);
        when(transactionRepository.findBySenderAccountIdAndIdempotencyKey(senderAccountId, idempotencyKey)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transferService.processTransfer(idempotencyKey, userId, transferRequest, "corr-123");

        assertNotNull(response);
        assertEquals(senderAccountId, response.getSenderAccountId());
        assertEquals(receiverAccountId, response.getReceiverAccountId());
        assertEquals(new BigDecimal("2500.0000"), response.getAmount());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());

        verify(accountBalanceService, times(1)).executeDebit(senderAccountId, new BigDecimal("2500.0000"));
        verify(accountBalanceService, times(1)).executeCredit(receiverAccountId, new BigDecimal("2500.0000"));
        verify(idempotencyService, times(1)).markCompleted(eq(idempotencyRecord), any(), eq(200), eq("COMPLETED"));
        verify(metricsService, times(1)).incrementTransactionsCompleted();
    }

    @Test
    @DisplayName("Integration Test 2: Idempotency Key Re-submission returns identical completed response")
    void testIdempotencyKeyResubmission_ReturnsCachedResponse() {
        UUID existingTxId = UUID.randomUUID();
        IdempotencyRecordEntity existingRecord = IdempotencyRecordEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .requestHash("payload_hash_777")
                .transactionId(existingTxId)
                .status(IdempotencyStatus.COMPLETED)
                .build();

        TransactionEntity existingTx = TransactionEntity.builder()
                .id(existingTxId)
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("2500.0000"))
                .currency("INR")
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash_777");
        when(idempotencyService.checkExistingRecord(eq(userId), eq(idempotencyKey), anyString())).thenReturn(Optional.of(existingRecord));
        when(transactionRepository.findById(existingTxId)).thenReturn(Optional.of(existingTx));
        when(transactionStepRepository.findByTransactionIdOrderByStepOrderAsc(existingTxId)).thenReturn(Collections.emptyList());

        TransactionResponse response = transferService.processTransfer(idempotencyKey, userId, transferRequest, "corr-123");

        assertNotNull(response);
        assertEquals(existingTxId, response.getId());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());

        verify(accountBalanceService, never()).executeDebit(any(), any());
        verify(accountBalanceService, never()).executeCredit(any(), any());
        verify(metricsService, times(1)).incrementIdempotencyHits();
    }

    @Test
    @DisplayName("Integration Test 3: Saga Failure Recovery on Credit Failure reverses debit")
    void testSagaFailureRecovery_ReversesDebit() {
        TransactionEntity savedTx = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("2500.0000"))
                .currency("INR")
                .status(TransactionStatus.INITIATED)
                .build();

        IdempotencyRecordEntity idempotencyRecord = IdempotencyRecordEntity.builder()
                .id(UUID.randomUUID())
                .status(IdempotencyStatus.PROCESSING)
                .build();

        when(idempotencyService.calculatePayloadHash(any())).thenReturn("payload_hash");
        when(idempotencyService.checkExistingRecord(any(), any(), any())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(any(), any(), any())).thenReturn(idempotencyRecord);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        when(transactionStepRepository.save(any(TransactionStepEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionException ex = assertThrows(TransactionException.class, () ->
                transferService.processTransfer(idempotencyKey, userId, transferRequest, "corr-123", "FAIL_CREDIT"));

        assertEquals(ErrorCode.SAGA_FAILED, ex.getErrorCode());
        verify(accountBalanceService, times(1)).executeDebit(senderAccountId, new BigDecimal("2500.0000"));
        verify(accountBalanceService, times(1)).executeCompensationDebit(senderAccountId, new BigDecimal("2500.0000"));
        verify(idempotencyService, times(1)).markCompleted(eq(idempotencyRecord), any(), eq(422), contains("COMPENSATED"));
        verify(metricsService, times(1)).incrementSagaCompensation();
    }

    @Test
    @DisplayName("Integration Test 4: Concurrent Transfers from Single Account under High Contention")
    void testConcurrentTransfersHighContention() throws InterruptedException {
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThreads);
        ConcurrentLinkedQueue<TransactionResponse> responses = new ConcurrentLinkedQueue<>();

        when(idempotencyService.calculatePayloadHash(any())).thenAnswer(inv -> "hash-" + UUID.randomUUID());
        when(idempotencyService.checkExistingRecord(any(), any(), any())).thenReturn(Optional.empty());
        when(idempotencyService.createRecord(any(), any(), any())).thenAnswer(inv -> IdempotencyRecordEntity.builder().id(UUID.randomUUID()).status(IdempotencyStatus.PROCESSING).build());
        when(transactionRepository.findBySenderAccountIdAndIdempotencyKey(any(), any())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            TransactionEntity tx = inv.getArgument(0);
            if (tx.getId() == null) {
                tx.setId(UUID.randomUUID());
            }
            return tx;
        });

        for (int i = 0; i < numThreads; i++) {
            final String threadKey = idempotencyKey + "-" + i;
            executorService.submit(() -> {
                try {
                    latch.await();
                    TransactionResponse res = transferService.processTransfer(threadKey, userId, transferRequest, "corr-" + threadKey);
                    responses.add(res);
                } catch (Exception e) {
                    // Handled
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        latch.countDown(); // Start all threads concurrently
        boolean finished = completionLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(finished);
        assertEquals(numThreads, responses.size());
    }
}
