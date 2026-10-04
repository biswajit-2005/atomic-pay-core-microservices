package com.atomicpay.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private UUID transactionId;
    private String type;
    private String title;
    private String message;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(UUID id, UUID userId, UUID transactionId, String type, String title, String message, OffsetDateTime readAt, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.transactionId = transactionId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getTransactionId() { return transactionId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private UUID transactionId;
        private String type;
        private String title;
        private String message;
        private OffsetDateTime readAt;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder readAt(OffsetDateTime readAt) { this.readAt = readAt; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public NotificationResponse build() {
            return new NotificationResponse(id, userId, transactionId, type, title, message, readAt, createdAt);
        }
    }
}
