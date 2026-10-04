package com.atomicpay.audit.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", schema = "audit")
public class AuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public AuditLogEntity() {}

    public AuditLogEntity(UUID id, UUID eventId, UUID actorUserId, UUID transactionId, String action, String entityType, UUID entityId, String metadata, String ipAddress, String userAgent, String correlationId, OffsetDateTime createdAt) {
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
    public void setId(UUID id) { this.id = id; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getActorUserId() { return actorUserId; }
    public void setActorUserId(UUID actorUserId) { this.actorUserId = actorUserId; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

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

        public AuditLogEntity build() {
            return new AuditLogEntity(id, eventId, actorUserId, transactionId, action, entityType, entityId, metadata, ipAddress, userAgent, correlationId, createdAt);
        }
    }
}
