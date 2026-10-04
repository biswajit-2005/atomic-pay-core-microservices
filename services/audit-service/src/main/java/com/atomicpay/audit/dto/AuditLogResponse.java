package com.atomicpay.audit.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditLogResponse {

    private UUID id;
    private UUID eventId;
    private UUID actorUserId;
    private UUID transactionId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private String correlationId;
    private OffsetDateTime createdAt;

    public AuditLogResponse() {}

    public AuditLogResponse(UUID id, UUID eventId, UUID actorUserId, UUID transactionId, String action, String entityType, UUID entityId, String metadata, String ipAddress, String userAgent, String correlationId, OffsetDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.actorUserId = actorUserId;
        this.transactionId = transactionId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getActorUserId() { return actorUserId; }
    public UUID getTransactionId() { return transactionId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getMetadata() { return metadata; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID id;
        private UUID eventId;
        private UUID actorUserId;
        private UUID transactionId;
        private String action;
        private String entityType;
        private UUID entityId;
        private String metadata;
        private String ipAddress;
        private String userAgent;
        private String correlationId;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder eventId(UUID eventId) { this.eventId = eventId; return this; }
        public Builder actorUserId(UUID actorUserId) { this.actorUserId = actorUserId; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(UUID entityId) { this.entityId = entityId; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AuditLogResponse build() {
            return new AuditLogResponse(id, eventId, actorUserId, transactionId, action, entityType, entityId, metadata, ipAddress, userAgent, correlationId, createdAt);
        }
    }
}
