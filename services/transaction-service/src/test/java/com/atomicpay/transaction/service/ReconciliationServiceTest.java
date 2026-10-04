package com.atomicpay.transaction.service;

import com.atomicpay.transaction.dto.ReconciliationIssueResponse;
import com.atomicpay.transaction.dto.ReconciliationRunResponse;
import com.atomicpay.transaction.entity.ReconciliationIssueEntity;
import com.atomicpay.transaction.entity.ReconciliationRunEntity;
import com.atomicpay.transaction.repository.ReconciliationIssueRepository;
import com.atomicpay.transaction.repository.ReconciliationRunRepository;
import com.atomicpay.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ReconciliationRunRepository reconciliationRunRepository;

    @Mock
    private ReconciliationIssueRepository reconciliationIssueRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.getResultList()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Should successfully execute reconciliation run with 0 mismatches")
    void runReconciliation_CleanRun_Success() {
        when(transactionRepository.count()).thenReturn(10L);
        when(reconciliationRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReconciliationRunResponse response = reconciliationService.runReconciliation();

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(10, response.getTotalTransactions());
        assertEquals(0, response.getMismatches());
        verify(reconciliationRunRepository, times(2)).save(any(ReconciliationRunEntity.class));
    }

    @Test
    @DisplayName("Should fetch reconciliation issues filtered by status")
    void getIssues_Success() {
        ReconciliationIssueEntity issue = ReconciliationIssueEntity.builder()
                .id(UUID.randomUUID())
                .runId(UUID.randomUUID())
                .issueType("LEDGER_BALANCE_MISMATCH")
                .status("OPEN")
                .createdAt(OffsetDateTime.now())
                .build();

        when(reconciliationIssueRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(List.of(issue));

        List<ReconciliationIssueResponse> response = reconciliationService.getIssues("OPEN");

        assertEquals(1, response.size());
        assertEquals("LEDGER_BALANCE_MISMATCH", response.get(0).getIssueType());
    }

    @Test
    @DisplayName("Should successfully resolve a reconciliation issue")
    void resolveIssue_Success() {
        UUID issueId = UUID.randomUUID();
        ReconciliationIssueEntity issue = ReconciliationIssueEntity.builder()
                .id(issueId)
                .runId(UUID.randomUUID())
                .issueType("PENDING_TRANSACTION")
                .status("OPEN")
                .createdAt(OffsetDateTime.now())
                .build();

        when(reconciliationIssueRepository.findById(issueId)).thenReturn(Optional.of(issue));
        when(reconciliationIssueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReconciliationIssueResponse response = reconciliationService.resolveIssue(issueId, "Manually refunded sender account");

        assertEquals("RESOLVED", response.getStatus());
        assertEquals("Manually refunded sender account", response.getResolution());
        assertNotNull(response.getResolvedAt());
    }
}
