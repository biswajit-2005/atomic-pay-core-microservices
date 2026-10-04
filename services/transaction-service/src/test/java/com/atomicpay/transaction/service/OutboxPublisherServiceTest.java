package com.atomicpay.transaction.service;

import com.atomicpay.transaction.entity.OutboxEventEntity;
import com.atomicpay.transaction.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        transactionId = UUID.randomUUID();
        senderAccountId = UUID.randomUUID();
        receiverAccountId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should successfully record outbox event in database with PENDING status")
    void recordOutboxEvent_Success() {
        outboxPublisherService.recordOutboxEvent(
                transactionId, "TRANSACTION_COMPLETED", "cid-123",
                senderAccountId, receiverAccountId, new BigDecimal("1000.0000"), "INR", "COMPLETED", null
        );

        verify(outboxEventRepository, times(1)).save(argThat(event -> {
            assertNotNull(event.getId());
            assertEquals("TRANSACTION", event.getAggregateType());
            assertEquals(transactionId, event.getAggregateId());
            assertEquals("TRANSACTION_COMPLETED", event.getEventType());
            assertEquals("PENDING", event.getStatus());
            return true;
        }));
    }

    @Test
    @DisplayName("Should publish pending outbox events to Kafka and mark as PUBLISHED")
    void publishPendingEvents_Success() {
        OutboxEventEntity pendingEvent = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("TRANSACTION")
                .aggregateId(transactionId)
                .eventType("TRANSACTION_COMPLETED")
                .payload("{\"eventId\":\"123\"}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(OffsetDateTime.now())
                .build();

        when(outboxEventRepository.findPendingEventsToPublish(any(OffsetDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(pendingEvent));
        when(kafkaTemplate.send(eq("transaction.events"), eq(transactionId.toString()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxPublisherService.publishPendingEvents();

        verify(kafkaTemplate, times(1)).send(eq("transaction.events"), eq(transactionId.toString()), anyString());
    }
}
