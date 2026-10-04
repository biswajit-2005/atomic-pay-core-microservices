package com.atomicpay.notification.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events", schema = "notifications")
@IdClass(ProcessedEventId.class)
public class ProcessedEventEntity {

    @Id
    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    public ProcessedEventEntity() {}

    public ProcessedEventEntity(String consumerName, UUID eventId, OffsetDateTime processedAt) {
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String consumerName) { this.consumerName = consumerName; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }

    @PrePersist
    public void prePersist() {
        if (processedAt == null) processedAt = OffsetDateTime.now();
    }
}
