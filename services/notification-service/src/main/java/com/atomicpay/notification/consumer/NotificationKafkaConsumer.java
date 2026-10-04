package com.atomicpay.notification.consumer;

import com.atomicpay.notification.dto.TransactionDomainEvent;
import com.atomicpay.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @KafkaListener(topics = "transaction.events", groupId = "notification-service")
    public void consumeTransactionEvent(String message) {
        log.info("NotificationKafkaConsumer received event payload from Kafka");
        try {
            TransactionDomainEvent event = objectMapper.readValue(message, TransactionDomainEvent.class);
            notificationService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process transaction event in NotificationKafkaConsumer: {}", message, e);
        }
    }
}
