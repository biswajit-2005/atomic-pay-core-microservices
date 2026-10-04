package com.atomicpay.audit.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TransactionDomainEvent {

    private UUID eventId;
    private String eventType;
    private int eventVersion;
    private UUID transactionId;
    private String correlationId;
    private OffsetDateTime occurredAt;
    private EventPayload payload;

    public TransactionDomainEvent() {}

    public TransactionDomainEvent(UUID eventId, String eventType, int eventVersion, UUID transactionId, String correlationId, OffsetDateTime occurredAt, EventPayload payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.transactionId = transactionId;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }

    public EventPayload getPayload() { return payload; }
    public void setPayload(EventPayload payload) { this.payload = payload; }

    public static class EventPayload {
        private UUID senderAccountId;
        private UUID receiverAccountId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String failureReason;

        public EventPayload() {}

        public EventPayload(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String currency, String status, String failureReason) {
            this.senderAccountId = senderAccountId;
            this.receiverAccountId = receiverAccountId;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
            this.failureReason = failureReason;
        }

        public UUID getSenderAccountId() { return senderAccountId; }
        public void setSenderAccountId(UUID senderAccountId) { this.senderAccountId = senderAccountId; }

        public UUID getReceiverAccountId() { return receiverAccountId; }
        public void setReceiverAccountId(UUID receiverAccountId) { this.receiverAccountId = receiverAccountId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }
}
