package com.atomicpay.audit.service;

import com.atomicpay.audit.dto.AuditLogResponse;
import com.atomicpay.audit.dto.TransactionDomainEvent;
import com.atomicpay.audit.entity.AuditLogEntity;
import com.atomicpay.audit.entity.ProcessedEventEntity;
import com.atomicpay.audit.entity.ProcessedEventId;
import com.atomicpay.audit.repository.AuditLogRepository;
import com.atomicpay.audit.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final String CONSUMER_NAME = "audit-service";

    private final AuditLogRepository auditLogRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ProcessedEventRepository processedEventRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Transactional
    public void processTransactionEvent(TransactionDomainEvent event) {
        if (event == null || event.getEventId() == null) {
            log.warn("Received null or invalid event in audit service");
            return;
        }

        ProcessedEventId processedEventId = new ProcessedEventId(CONSUMER_NAME, event.getEventId());
        if (processedEventRepository.existsById(processedEventId)) {
            log.info("Event {} has already been processed by {}. Deduplicating and ignoring.", event.getEventId(), CONSUMER_NAME);
            return;
        }

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(event.getPayload());
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .eventId(event.getEventId())
                .actorUserId(event.getPayload() != null ? event.getPayload().getSenderAccountId() : null)
                .transactionId(event.getTransactionId())
                .action(event.getEventType() != null ? event.getEventType() : "TRANSACTION_EVENT")
                .entityType("TRANSACTION")
                .entityId(event.getTransactionId())
                .metadata(metadataJson)
                .correlationId(event.getCorrelationId())
                .createdAt(event.getOccurredAt() != null ? event.getOccurredAt() : OffsetDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        // Mark event as processed (Inbox Deduplication)
        processedEventRepository.save(new ProcessedEventEntity(CONSUMER_NAME, event.getEventId(), OffsetDateTime.now()));
        log.info("Successfully recorded audit log {} for transaction event {}", auditLog.getId(), event.getEventId());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsForTransaction(UUID transactionId) {
        return auditLogRepository.findByTransactionIdOrderByCreatedAtDesc(transactionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLogEntity entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .eventId(entity.getEventId())
                .actorUserId(entity.getActorUserId())
                .transactionId(entity.getTransactionId())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .metadata(entity.getMetadata())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .correlationId(entity.getCorrelationId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
