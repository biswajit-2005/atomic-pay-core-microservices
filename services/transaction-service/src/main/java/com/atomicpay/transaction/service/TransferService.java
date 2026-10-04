package com.atomicpay.transaction.service;

import com.atomicpay.transaction.client.AccountBalanceService;
import com.atomicpay.transaction.domain.*;
import com.atomicpay.transaction.dto.*;
import com.atomicpay.transaction.entity.IdempotencyRecordEntity;
import com.atomicpay.transaction.entity.TransactionEntity;
import com.atomicpay.transaction.entity.TransactionStepEntity;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.repository.TransactionRepository;
import com.atomicpay.transaction.repository.TransactionStepRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionStepRepository transactionStepRepository;
    private final IdempotencyService idempotencyService;
    private final AccountBalanceService accountBalanceService;
    private final EntityManager entityManager;

    public TransferService(
            TransactionRepository transactionRepository,
            TransactionStepRepository transactionStepRepository,
            IdempotencyService idempotencyService,
            AccountBalanceService accountBalanceService,
            EntityManager entityManager
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionStepRepository = transactionStepRepository;
        this.idempotencyService = idempotencyService;
        this.accountBalanceService = accountBalanceService;
        this.entityManager = entityManager;
    }

    @Transactional
    public TransactionResponse processTransfer(String idempotencyKey, UUID userId, TransferRequest request, String correlationId) {
        return processTransfer(idempotencyKey, userId, request, correlationId, null);
    }

    @Transactional
    public TransactionResponse processTransfer(String idempotencyKey, UUID userId, TransferRequest request, String correlationId, String simulateFailure) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new TransactionException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        String payloadHash = idempotencyService.calculatePayloadHash(request);

        // Check for existing idempotency record
        Optional<IdempotencyRecordEntity> existingRecord = idempotencyService.checkExistingRecord(userId, idempotencyKey, payloadHash);
        if (existingRecord.isPresent()) {
            IdempotencyRecordEntity record = existingRecord.get();
            if (record.getTransactionId() != null) {
                return getTransaction(record.getTransactionId());
            }
        }

        IdempotencyRecordEntity idempotencyRecord = idempotencyService.createRecord(userId, idempotencyKey, payloadHash);

        // Check DB unique constraint backup (senderAccountId, idempotencyKey)
        Optional<TransactionEntity> existingTx = transactionRepository.findBySenderAccountIdAndIdempotencyKey(request.getSenderAccountId(), idempotencyKey);
        if (existingTx.isPresent()) {
            TransactionEntity tx = existingTx.get();
            idempotencyService.markCompleted(idempotencyRecord, tx.getId(), 200, tx.getStatus().name());
            return mapToTransactionResponse(tx, transactionStepRepository.findByTransactionIdOrderByStepOrderAsc(tx.getId()));
        }

        // Initialize Transaction State
        TransactionEntity transaction = TransactionEntity.builder()
                .idempotencyKey(idempotencyKey)
                .senderAccountId(request.getSenderAccountId())
                .receiverAccountId(request.getReceiverAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(TransactionStatus.INITIATED)
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .build();

        TransactionEntity savedTx = transactionRepository.save(transaction);

        // Saga Step 1: VALIDATE
        TransactionStepEntity validateStep = createStep(savedTx.getId(), 1, StepType.VALIDATE);
        transitionState(savedTx, TransactionStatus.VALIDATING);

        try {
            validateTransferRequest(request);
            completeStepSuccess(validateStep);
        } catch (TransactionException e) {
            failStep(validateStep, e.getErrorCode().name(), e.getMessage());
            failTransaction(savedTx, e.getErrorCode().name(), e.getMessage());
            idempotencyService.markCompleted(idempotencyRecord, savedTx.getId(), e.getErrorCode().getHttpStatus().value(), e.getMessage());
            throw e;
        }

        // Transition to PROCESSING
        transitionState(savedTx, TransactionStatus.PROCESSING);

        // Saga Step 2: DEBIT
        TransactionStepEntity debitStep = createStep(savedTx.getId(), 2, StepType.DEBIT);
        if ("FAIL_DEBIT".equalsIgnoreCase(simulateFailure)) {
            failStep(debitStep, ErrorCode.SAGA_FAILED.name(), "Simulated DEBIT failure");
            failTransaction(savedTx, ErrorCode.SAGA_FAILED.name(), "Simulated DEBIT failure");
            idempotencyService.markCompleted(idempotencyRecord, savedTx.getId(), 422, "Simulated DEBIT failure");
            throw new TransactionException(ErrorCode.SAGA_FAILED, "Simulated DEBIT failure");
        }

        try {
            accountBalanceService.executeDebit(request.getSenderAccountId(), request.getAmount());
            completeStepSuccess(debitStep);
            transitionState(savedTx, TransactionStatus.DEBITED);
        } catch (TransactionException e) {
            failStep(debitStep, e.getErrorCode().name(), e.getMessage());
            failTransaction(savedTx, e.getErrorCode().name(), e.getMessage());
            idempotencyService.markCompleted(idempotencyRecord, savedTx.getId(), e.getErrorCode().getHttpStatus().value(), e.getMessage());
            throw e;
        }

        // Saga Step 3: CREDIT
        TransactionStepEntity creditStep = createStep(savedTx.getId(), 3, StepType.CREDIT);
        if ("FAIL_CREDIT".equalsIgnoreCase(simulateFailure)) {
            failStep(creditStep, ErrorCode.SAGA_FAILED.name(), "Simulated CREDIT failure");
            compensateDebit(savedTx, idempotencyRecord, request, "Simulated CREDIT failure");
            throw new TransactionException(ErrorCode.SAGA_FAILED, "Simulated CREDIT failure triggering compensation");
        } else if ("DELAY_CREDIT".equalsIgnoreCase(simulateFailure)) {
            failStep(creditStep, ErrorCode.TRANSACTION_PENDING.name(), "Simulated CREDIT timeout");
            handlePendingOutcome(savedTx, idempotencyRecord, "Simulated CREDIT timeout");
            throw new TransactionException(ErrorCode.TRANSACTION_PENDING, "Transaction pending reconciliation due to simulated credit timeout");
        }

        try {
            accountBalanceService.executeCredit(request.getReceiverAccountId(), request.getAmount());
            completeStepSuccess(creditStep);
            transitionState(savedTx, TransactionStatus.CREDITED);
        } catch (Exception e) {
            log.error("Credit step failed for transaction {}, triggering Saga compensation", savedTx.getId(), e);
            failStep(creditStep, ErrorCode.SAGA_FAILED.name(), e.getMessage());
            compensateDebit(savedTx, idempotencyRecord, request, "Credit step failed: " + e.getMessage());
            throw new TransactionException(ErrorCode.SAGA_FAILED, "Credit step failed, sender account compensated");
        }

        // Saga Step 4: LEDGER
        TransactionStepEntity ledgerStep = createStep(savedTx.getId(), 4, StepType.LEDGER);
        if ("FAIL_LEDGER".equalsIgnoreCase(simulateFailure)) {
            failStep(ledgerStep, ErrorCode.SAGA_FAILED.name(), "Simulated LEDGER failure");
            compensateFullTransfer(savedTx, idempotencyRecord, request, "Simulated LEDGER failure");
            throw new TransactionException(ErrorCode.SAGA_FAILED, "Simulated LEDGER failure triggering compensation");
        }

        try {
            recordLedgerDoubleEntry(savedTx.getId(), request.getSenderAccountId(), request.getReceiverAccountId(), request.getAmount(), request.getCurrency() != null ? request.getCurrency() : "INR");
            completeStepSuccess(ledgerStep);
        } catch (Exception e) {
            log.error("Ledger step failed for transaction {}, triggering full Saga compensation", savedTx.getId(), e);
            failStep(ledgerStep, ErrorCode.SAGA_FAILED.name(), e.getMessage());
            compensateFullTransfer(savedTx, idempotencyRecord, request, "Ledger step failed: " + e.getMessage());
            throw new TransactionException(ErrorCode.SAGA_FAILED, "Ledger step failed, transfer compensated");
        }

        // Saga Step 5: COMPLETE
        transitionState(savedTx, TransactionStatus.COMPLETED);
        savedTx.setCompletedAt(OffsetDateTime.now());
        TransactionStepEntity completeStep = createStep(savedTx.getId(), 5, StepType.COMPLETE);
        completeStepSuccess(completeStep);

        TransactionEntity finalTx = transactionRepository.save(savedTx);
        idempotencyService.markCompleted(idempotencyRecord, finalTx.getId(), 200, "COMPLETED");

        log.info("Transaction {} completed successfully. Debited {} from {} to {}", finalTx.getId(), finalTx.getAmount(), finalTx.getSenderAccountId(), finalTx.getReceiverAccountId());

        List<TransactionStepEntity> steps = transactionStepRepository.findByTransactionIdOrderByStepOrderAsc(finalTx.getId());
        return mapToTransactionResponse(finalTx, steps);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId) {
        TransactionEntity tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND, "Transaction not found with ID: " + transactionId));
        List<TransactionStepEntity> steps = transactionStepRepository.findByTransactionIdOrderByStepOrderAsc(tx.getId());
        return mapToTransactionResponse(tx, steps);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForAccount(UUID accountId) {
        return transactionRepository.findBySenderAccountIdOrReceiverAccountIdOrderByCreatedAtDesc(accountId, accountId).stream()
                .map(tx -> {
                    List<TransactionStepEntity> steps = transactionStepRepository.findByTransactionIdOrderByStepOrderAsc(tx.getId());
                    return mapToTransactionResponse(tx, steps);
                })
                .collect(Collectors.toList());
    }

    private void recordLedgerDoubleEntry(UUID transactionId, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String currency) {
        Timestamp now = Timestamp.from(OffsetDateTime.now().toInstant());

        // Ensure ledger account for sender exists
        entityManager.createNativeQuery(
                "INSERT INTO ledger.ledger_accounts (id, account_id, created_at) VALUES (:id, :accountId, :now) ON CONFLICT (account_id) DO NOTHING")
                .setParameter("id", UUID.randomUUID())
                .setParameter("accountId", senderAccountId)
                .setParameter("now", now)
                .executeUpdate();

        // Ensure ledger account for receiver exists
        entityManager.createNativeQuery(
                "INSERT INTO ledger.ledger_accounts (id, account_id, created_at) VALUES (:id, :accountId, :now) ON CONFLICT (account_id) DO NOTHING")
                .setParameter("id", UUID.randomUUID())
                .setParameter("accountId", receiverAccountId)
                .setParameter("now", now)
                .executeUpdate();

        // Insert DEBIT entry for sender
        entityManager.createNativeQuery(
                "INSERT INTO ledger.ledger_entries (id, transaction_id, ledger_account_id, entry_type, amount, currency, sequence_no, created_at) " +
                        "SELECT :entryId, :txId, id, 'DEBIT', :amount, :currency, 1, :now FROM ledger.ledger_accounts WHERE account_id = :senderId")
                .setParameter("entryId", UUID.randomUUID())
                .setParameter("txId", transactionId)
                .setParameter("amount", amount)
                .setParameter("currency", currency)
                .setParameter("now", now)
                .setParameter("senderId", senderAccountId)
                .executeUpdate();

        // Insert CREDIT entry for receiver
        entityManager.createNativeQuery(
                "INSERT INTO ledger.ledger_entries (id, transaction_id, ledger_account_id, entry_type, amount, currency, sequence_no, created_at) " +
                        "SELECT :entryId, :txId, id, 'CREDIT', :amount, :currency, 2, :now FROM ledger.ledger_accounts WHERE account_id = :receiverId")
                .setParameter("entryId", UUID.randomUUID())
                .setParameter("txId", transactionId)
                .setParameter("amount", amount)
                .setParameter("currency", currency)
                .setParameter("now", now)
                .setParameter("receiverId", receiverAccountId)
                .executeUpdate();
    }

    private void compensateDebit(TransactionEntity tx, IdempotencyRecordEntity idempotencyRecord, TransferRequest request, String reason) {
        log.warn("Initiating Saga compensation (reversing DEBIT) for transaction {}", tx.getId());
        transitionState(tx, TransactionStatus.COMPENSATING);
        TransactionStepEntity compensateStep = createStep(tx.getId(), 10, StepType.COMPENSATE_DEBIT);
        try {
            accountBalanceService.executeCompensationDebit(request.getSenderAccountId(), request.getAmount());
            completeStepSuccess(compensateStep);
            transitionState(tx, TransactionStatus.COMPENSATED);
            tx.setFailureCode(ErrorCode.SAGA_FAILED.name());
            tx.setFailureReason(reason);
            transactionRepository.save(tx);
            idempotencyService.markCompleted(idempotencyRecord, tx.getId(), 422, "COMPENSATED: " + reason);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to compensate debit for transaction {}", tx.getId(), e);
            failStep(compensateStep, ErrorCode.INTERNAL_ERROR.name(), e.getMessage());
            handlePendingOutcome(tx, idempotencyRecord, "Compensation failed: " + e.getMessage());
        }
    }

    private void compensateFullTransfer(TransactionEntity tx, IdempotencyRecordEntity idempotencyRecord, TransferRequest request, String reason) {
        log.warn("Initiating full Saga compensation (reversing DEBIT and CREDIT) for transaction {}", tx.getId());
        transitionState(tx, TransactionStatus.COMPENSATING);
        TransactionStepEntity compensateStep = createStep(tx.getId(), 10, StepType.COMPENSATE_DEBIT);
        try {
            accountBalanceService.executeCompensationCredit(request.getReceiverAccountId(), request.getAmount());
            accountBalanceService.executeCompensationDebit(request.getSenderAccountId(), request.getAmount());
            completeStepSuccess(compensateStep);
            transitionState(tx, TransactionStatus.COMPENSATED);
            tx.setFailureCode(ErrorCode.SAGA_FAILED.name());
            tx.setFailureReason(reason);
            transactionRepository.save(tx);
            idempotencyService.markCompleted(idempotencyRecord, tx.getId(), 422, "COMPENSATED: " + reason);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to compensate full transfer for transaction {}", tx.getId(), e);
            failStep(compensateStep, ErrorCode.INTERNAL_ERROR.name(), e.getMessage());
            handlePendingOutcome(tx, idempotencyRecord, "Full compensation failed: " + e.getMessage());
        }
    }

    private void handlePendingOutcome(TransactionEntity tx, IdempotencyRecordEntity idempotencyRecord, String reason) {
        log.warn("Transaction {} outcome is pending/uncertain. Marking for RECONCILIATION_REQUIRED", tx.getId());
        transitionState(tx, TransactionStatus.PENDING);
        transitionState(tx, TransactionStatus.RECONCILIATION_REQUIRED);
        tx.setFailureCode(ErrorCode.RECONCILIATION_REQUIRED.name());
        tx.setFailureReason(reason);
        transactionRepository.save(tx);
        idempotencyService.markCompleted(idempotencyRecord, tx.getId(), 504, "RECONCILIATION_REQUIRED: " + reason);
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request.getSenderAccountId().equals(request.getReceiverAccountId())) {
            throw new TransactionException(ErrorCode.SELF_TRANSFER_NOT_ALLOWED, "Sender and receiver accounts must be different");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionException(ErrorCode.INVALID_AMOUNT, "Transfer amount must be greater than zero");
        }
        if (request.getCurrency() != null && !"INR".equalsIgnoreCase(request.getCurrency())) {
            throw new TransactionException(ErrorCode.CURRENCY_NOT_SUPPORTED, "Only INR currency is supported");
        }
    }

    private void transitionState(TransactionEntity tx, TransactionStatus nextStatus) {
        TransactionStateMachine.validateTransition(tx.getStatus(), nextStatus);
        tx.setStatus(nextStatus);
        transactionRepository.save(tx);
    }

    private TransactionStepEntity createStep(UUID transactionId, int stepOrder, StepType stepType) {
        TransactionStepEntity step = TransactionStepEntity.builder()
                .transactionId(transactionId)
                .stepOrder(stepOrder)
                .stepType(stepType)
                .status(StepStatus.IN_PROGRESS)
                .startedAt(OffsetDateTime.now())
                .attempts(1)
                .build();
        TransactionStepEntity saved = transactionStepRepository.save(step);
        return saved != null ? saved : step;
    }

    private void completeStepSuccess(TransactionStepEntity step) {
        if (step != null) {
            step.setStatus(StepStatus.SUCCESS);
            step.setCompletedAt(OffsetDateTime.now());
            transactionStepRepository.save(step);
        }
    }

    private void failStep(TransactionStepEntity step, String errorCode, String errorMessage) {
        if (step != null) {
            step.setStatus(StepStatus.FAILED);
            step.setErrorCode(errorCode);
            step.setErrorMessage(errorMessage);
            step.setCompletedAt(OffsetDateTime.now());
            transactionStepRepository.save(step);
        }
    }

    private void failTransaction(TransactionEntity tx, String failureCode, String failureReason) {
        if (TransactionStateMachine.isValidTransition(tx.getStatus(), TransactionStatus.FAILED)) {
            tx.setStatus(TransactionStatus.FAILED);
        }
        tx.setFailureCode(failureCode);
        tx.setFailureReason(failureReason);
        transactionRepository.save(tx);
    }

    private TransactionResponse mapToTransactionResponse(TransactionEntity tx, List<TransactionStepEntity> steps) {
        List<TransactionStepResponse> stepResponses = (steps != null ? steps : Collections.<TransactionStepEntity>emptyList()).stream()
                .map(s -> TransactionStepResponse.builder()
                        .id(s.getId())
                        .stepOrder(s.getStepOrder())
                        .stepType(s.getStepType())
                        .status(s.getStatus())
                        .attempts(s.getAttempts())
                        .startedAt(s.getStartedAt())
                        .completedAt(s.getCompletedAt())
                        .errorCode(s.getErrorCode())
                        .errorMessage(s.getErrorMessage())
                        .build())
                .collect(Collectors.toList());

        return TransactionResponse.builder()
                .id(tx.getId())
                .idempotencyKey(tx.getIdempotencyKey())
                .senderAccountId(tx.getSenderAccountId())
                .receiverAccountId(tx.getReceiverAccountId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus())
                .failureCode(tx.getFailureCode())
                .failureReason(tx.getFailureReason())
                .correlationId(tx.getCorrelationId())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .completedAt(tx.getCompletedAt())
                .steps(stepResponses)
                .build();
    }
}
