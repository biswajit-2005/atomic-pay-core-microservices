package com.atomicpay.transaction.service;

import com.atomicpay.transaction.dto.ReconciliationIssueResponse;
import com.atomicpay.transaction.dto.ReconciliationRunResponse;
import com.atomicpay.transaction.entity.ReconciliationIssueEntity;
import com.atomicpay.transaction.entity.ReconciliationRunEntity;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.repository.ReconciliationIssueRepository;
import com.atomicpay.transaction.repository.ReconciliationRunRepository;
import com.atomicpay.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final ReconciliationRunRepository reconciliationRunRepository;
    private final ReconciliationIssueRepository reconciliationIssueRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;

    public ReconciliationService(ReconciliationRunRepository reconciliationRunRepository,
                                 ReconciliationIssueRepository reconciliationIssueRepository,
                                 TransactionRepository transactionRepository,
                                 EntityManager entityManager) {
        this.reconciliationRunRepository = reconciliationRunRepository;
        this.reconciliationIssueRepository = reconciliationIssueRepository;
        this.transactionRepository = transactionRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public ReconciliationRunResponse runReconciliation() {
        log.info("Starting automated financial reconciliation run...");
        ReconciliationRunEntity run = ReconciliationRunEntity.builder()
                .id(UUID.randomUUID())
                .startedAt(OffsetDateTime.now())
                .status("RUNNING")
                .totalTransactions(0)
                .mismatches(0)
                .build();

        ReconciliationRunEntity savedRun = reconciliationRunRepository.save(run);
        int issueCount = 0;

        // Count total transactions processed
        long totalTransactionsCount = transactionRepository.count();

        // 1. Audit Check: Completed transactions missing or having duplicate ledger entries
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> txLedgerCounts = entityManager.createNativeQuery(
                    "SELECT t.id, COUNT(l.id) FROM transactions.transactions t " +
                            "LEFT JOIN ledger.ledger_entries l ON t.id = l.transaction_id " +
                            "WHERE t.status = 'COMPLETED' GROUP BY t.id HAVING COUNT(l.id) <> 2"
            ).getResultList();

            for (Object[] row : txLedgerCounts) {
                UUID txId = (UUID) row[0];
                long entryCount = ((Number) row[1]).longValue();
                String issueType = entryCount < 2 ? "MISSING_LEDGER_ENTRY" : "DUPLICATE_LEDGER_ENTRY";

                ReconciliationIssueEntity issue = ReconciliationIssueEntity.builder()
                        .id(UUID.randomUUID())
                        .runId(savedRun.getId())
                        .transactionId(txId)
                        .issueType(issueType)
                        .expectedValue(new BigDecimal("2"))
                        .actualValue(BigDecimal.valueOf(entryCount))
                        .status("OPEN")
                        .createdAt(OffsetDateTime.now())
                        .build();

                reconciliationIssueRepository.save(issue);
                issueCount++;
                log.warn("Reconciliation Run {}: Found {} for transaction {}", savedRun.getId(), issueType, txId);
            }
        } catch (Exception e) {
            log.error("Reconciliation Check 1 (Ledger Entry Count) failed", e);
        }

        // 2. Audit Check: Stuck Pending/Processing transactions older than 5 minutes
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> stuckTx = entityManager.createNativeQuery(
                    "SELECT id, status FROM transactions.transactions " +
                            "WHERE status IN ('PENDING', 'PROCESSING', 'RECONCILIATION_REQUIRED', 'COMPENSATING') " +
                            "AND created_at <= :threshold"
            ).setParameter("threshold", OffsetDateTime.now().minusMinutes(5)).getResultList();

            for (Object[] row : stuckTx) {
                UUID txId = (UUID) row[0];
                String txStatus = (String) row[1];

                ReconciliationIssueEntity issue = ReconciliationIssueEntity.builder()
                        .id(UUID.randomUUID())
                        .runId(savedRun.getId())
                        .transactionId(txId)
                        .issueType("PENDING_TRANSACTION")
                        .status("OPEN")
                        .resolution("Stuck in state: " + txStatus)
                        .createdAt(OffsetDateTime.now())
                        .build();

                reconciliationIssueRepository.save(issue);
                issueCount++;
                log.warn("Reconciliation Run {}: Found PENDING_TRANSACTION for tx {} in status {}", savedRun.getId(), txId, txStatus);
            }
        } catch (Exception e) {
            log.error("Reconciliation Check 2 (Stuck Pending Transactions) failed", e);
        }

        // 3. Audit Check: Account balance mismatch vs double-entry ledger calculation
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> balanceMismatches = entityManager.createNativeQuery(
                    "SELECT a.id, a.current_balance, " +
                            "COALESCE(SUM(CASE WHEN l.entry_type = 'CREDIT' THEN l.amount ELSE 0 END), 0) - " +
                            "COALESCE(SUM(CASE WHEN l.entry_type = 'DEBIT' THEN l.amount ELSE 0 END), 0) AS calculated_balance " +
                            "FROM accounts.accounts a " +
                            "LEFT JOIN ledger.ledger_accounts la ON a.id = la.account_id " +
                            "LEFT JOIN ledger.ledger_entries l ON la.id = l.ledger_account_id " +
                            "GROUP BY a.id, a.current_balance " +
                            "HAVING a.current_balance <> (COALESCE(SUM(CASE WHEN l.entry_type = 'CREDIT' THEN l.amount ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN l.entry_type = 'DEBIT' THEN l.amount ELSE 0 END), 0))"
            ).getResultList();

            for (Object[] row : balanceMismatches) {
                UUID accId = (UUID) row[0];
                BigDecimal actualBalance = (BigDecimal) row[1];
                BigDecimal expectedBalance = (BigDecimal) row[2];

                ReconciliationIssueEntity issue = ReconciliationIssueEntity.builder()
                        .id(UUID.randomUUID())
                        .runId(savedRun.getId())
                        .accountId(accId)
                        .issueType("LEDGER_BALANCE_MISMATCH")
                        .expectedValue(expectedBalance)
                        .actualValue(actualBalance)
                        .status("OPEN")
                        .createdAt(OffsetDateTime.now())
                        .build();

                reconciliationIssueRepository.save(issue);
                issueCount++;
                log.warn("Reconciliation Run {}: Balance mismatch for account {}. Current: {}, Calculated: {}", savedRun.getId(), accId, actualBalance, expectedBalance);
            }
        } catch (Exception e) {
            log.error("Reconciliation Check 3 (Ledger Balance Mismatch) failed", e);
        }

        savedRun.setCompletedAt(OffsetDateTime.now());
        savedRun.setStatus("COMPLETED");
        savedRun.setTotalTransactions((int) totalTransactionsCount);
        savedRun.setMismatches(issueCount);

        ReconciliationRunEntity finalRun = reconciliationRunRepository.save(savedRun);
        log.info("Completed financial reconciliation run {}. Total transactions evaluated: {}, Mismatches detected: {}", finalRun.getId(), totalTransactionsCount, issueCount);

        return mapToRunResponse(finalRun);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationIssueResponse> getIssues(String status) {
        List<ReconciliationIssueEntity> issues;
        if (status != null && !status.isBlank()) {
            issues = reconciliationIssueRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            issues = reconciliationIssueRepository.findAllByOrderByCreatedAtDesc();
        }
        return issues.stream().map(this::mapToIssueResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReconciliationIssueResponse resolveIssue(UUID issueId, String resolutionNotes) {
        ReconciliationIssueEntity issue = reconciliationIssueRepository.findById(issueId)
                .orElseThrow(() -> new TransactionException(ErrorCode.INTERNAL_ERROR, "Reconciliation issue not found: " + issueId));

        issue.setStatus("RESOLVED");
        issue.setResolution(resolutionNotes);
        issue.setResolvedAt(OffsetDateTime.now());

        ReconciliationIssueEntity updated = reconciliationIssueRepository.save(issue);
        log.info("Resolved reconciliation issue {} with notes: {}", issueId, resolutionNotes);
        return mapToIssueResponse(updated);
    }

    private ReconciliationRunResponse mapToRunResponse(ReconciliationRunEntity entity) {
        return ReconciliationRunResponse.builder()
                .id(entity.getId())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .status(entity.getStatus())
                .totalTransactions(entity.getTotalTransactions())
                .mismatches(entity.getMismatches())
                .build();
    }

    private ReconciliationIssueResponse mapToIssueResponse(ReconciliationIssueEntity entity) {
        return ReconciliationIssueResponse.builder()
                .id(entity.getId())
                .runId(entity.getRunId())
                .transactionId(entity.getTransactionId())
                .accountId(entity.getAccountId())
                .issueType(entity.getIssueType())
                .expectedValue(entity.getExpectedValue())
                .actualValue(entity.getActualValue())
                .status(entity.getStatus())
                .resolution(entity.getResolution())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
