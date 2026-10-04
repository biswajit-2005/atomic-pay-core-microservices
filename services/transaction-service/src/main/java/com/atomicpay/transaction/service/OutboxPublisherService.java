package com.atomicpay.transaction.service;

import com.atomicpay.transaction.dto.TransactionDomainEvent;
import com.atomicpay.transaction.entity.OutboxEventEntity;
import com.atomicpay.transaction.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@EnableScheduling
public class OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);
    public static final String TRANSACTION_EVENTS_TOPIC = "transaction.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisherService(OutboxEventRepository outboxEventRepository,
                                  KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional
    public void recordOutboxEvent(UUID transactionId, String eventType, String correlationId,
                                  UUID senderAccountId, UUID receiverAccountId,
                                  BigDecimal amount, String currency, String status, String failureReason) {
        try {
            UUID eventId = UUID.randomUUID();
            TransactionDomainEvent.EventPayload payload = new TransactionDomainEvent.EventPayload(
                    senderAccountId, receiverAccountId, amount, currency, status, failureReason
            );

            TransactionDomainEvent event = new TransactionDomainEvent(
                    eventId, eventType, 1, transactionId, correlationId, OffsetDateTime.now(), payload
            );

            String jsonPayload = objectMapper.writeValueAsString(event);

            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .id(eventId)
                    .aggregateType("TRANSACTION")
                    .aggregateId(transactionId)
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(OffsetDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Recorded transactional outbox event {} for transaction {}", eventId, transactionId);
        } catch (Exception e) {
            log.error("Failed to record outbox event for transaction {}", transactionId, e);
            throw new RuntimeException("Outbox recording failed", e);
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findPendingEventsToPublish(
                OffsetDateTime.now(), PageRequest.of(0, 50)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish to Kafka", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            try {
                String key = event.getAggregateId().toString();
                kafkaTemplate.send(TRANSACTION_EVENTS_TOPIC, key, event.getPayload()).whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus("PUBLISHED");
                        event.setPublishedAt(OffsetDateTime.now());
                        outboxEventRepository.save(event);
                        log.info("Successfully published outbox event {} to Kafka topic {}", event.getId(), TRANSACTION_EVENTS_TOPIC);
                    } else {
                        log.error("Failed to publish outbox event {} to Kafka", event.getId(), ex);
                        int retries = event.getRetryCount() + 1;
                        event.setRetryCount(retries);
                        event.setNextAttemptAt(OffsetDateTime.now().plusSeconds(retries * 5L));
                        if (retries >= 5) {
                            event.setStatus("FAILED");
                        }
                        outboxEventRepository.save(event);
                    }
                });
            } catch (Exception e) {
                log.error("Error initiating Kafka publish for outbox event {}", event.getId(), e);
            }
        }
    }
}
