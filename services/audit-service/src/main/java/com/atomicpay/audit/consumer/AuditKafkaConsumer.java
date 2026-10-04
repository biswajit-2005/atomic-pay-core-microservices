package com.atomicpay.audit.consumer;

import com.atomicpay.audit.dto.TransactionDomainEvent;
import com.atomicpay.audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaConsumer.class);

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditKafkaConsumer(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @KafkaListener(topics = "transaction.events", groupId = "audit-service")
    public void consumeTransactionEvent(String message) {
        log.info("AuditKafkaConsumer received event payload from Kafka");
        try {
            TransactionDomainEvent event = objectMapper.readValue(message, TransactionDomainEvent.class);
            auditService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process transaction event in AuditKafkaConsumer: {}", message, e);
        }
    }
}
