package com.atomicpay.transaction.entity;

import com.atomicpay.transaction.domain.IdempotencyStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records", schema = "transactions",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_idempotency", columnNames = {"user_id", "idempotency_key"}))
public class IdempotencyRecordEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public IdempotencyRecordEntity() {}

    public IdempotencyRecordEntity(UUID id, UUID userId, String idempotencyKey, String requestHash, UUID transactionId, Integer responseStatus, String responseBody, IdempotencyStatus status, OffsetDateTime expiresAt, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.transactionId = transactionId;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public IdempotencyStatus getStatus() { return status; }
    public void setStatus(IdempotencyStatus status) { this.status = status; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String idempotencyKey;
        private String requestHash;
        private UUID transactionId;
        private Integer responseStatus;
        private String responseBody;
        private IdempotencyStatus status;
        private OffsetDateTime expiresAt;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder requestHash(String requestHash) { this.requestHash = requestHash; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder responseStatus(Integer responseStatus) { this.responseStatus = responseStatus; return this; }
        public Builder responseBody(String responseBody) { this.responseBody = responseBody; return this; }
        public Builder status(IdempotencyStatus status) { this.status = status; return this; }
        public Builder expiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public IdempotencyRecordEntity build() {
            return new IdempotencyRecordEntity(id, userId, idempotencyKey, requestHash, transactionId, responseStatus, responseBody, status, expiresAt, createdAt);
        }
    }
}
