package com.atomicpay.audit.service;

import com.atomicpay.audit.dto.AuditLogResponse;
import com.atomicpay.audit.dto.TransactionDomainEvent;
import com.atomicpay.audit.entity.AuditLogEntity;
import com.atomicpay.audit.entity.ProcessedEventEntity;
import com.atomicpay.audit.entity.ProcessedEventId;
import com.atomicpay.audit.repository.AuditLogRepository;
import com.atomicpay.audit.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditService auditService;

    private UUID userId;
    private UUID eventId;
    private UUID transactionId;
    private TransactionDomainEvent event;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        TransactionDomainEvent.EventPayload payload = new TransactionDomainEvent.EventPayload(
                userId, UUID.randomUUID(), new BigDecimal("1000.0000"), "INR", "COMPLETED", null
        );

        event = new TransactionDomainEvent(
                eventId, "TRANSACTION_COMPLETED", 1, transactionId, "cid-123", OffsetDateTime.now(), payload
        );
    }

    @Test
    @DisplayName("Should create audit log and save processed event for new transaction event")
    void processTransactionEvent_NewEvent_Success() {
        when(processedEventRepository.existsById(any(ProcessedEventId.class))).thenReturn(false);

        auditService.processTransactionEvent(event);

        verify(auditLogRepository, times(1)).save(argThat(log -> {
            assertEquals(eventId, log.getEventId());
            assertEquals(transactionId, log.getTransactionId());
            assertEquals("TRANSACTION_COMPLETED", log.getAction());
            assertEquals("TRANSACTION", log.getEntityType());
            return true;
        }));

        verify(processedEventRepository, times(1)).save(any(ProcessedEventEntity.class));
    }

    @Test
    @DisplayName("Should deduplicate and ignore already processed audit event")
    void processTransactionEvent_DuplicateEvent_Ignored() {
        when(processedEventRepository.existsById(any(ProcessedEventId.class))).thenReturn(true);

        auditService.processTransactionEvent(event);

        verify(auditLogRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch audit logs for admin")
    void getAuditLogs_Success() {
        AuditLogEntity auditLog = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .transactionId(transactionId)
                .action("TRANSACTION_COMPLETED")
                .entityType("TRANSACTION")
                .createdAt(OffsetDateTime.now())
                .build();

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.getAuditLogs(0, 50);

        assertEquals(1, result.size());
        assertEquals("TRANSACTION_COMPLETED", result.get(0).getAction());
    }
}
