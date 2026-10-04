package com.atomicpay.transaction.entity;

import com.atomicpay.transaction.domain.TransactionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", schema = "transactions",
       uniqueConstraints = @UniqueConstraint(name = "uq_transaction_idempotency", columnNames = {"sender_account_id", "idempotency_key"}))
public class TransactionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "sender_account_id", nullable = false)
    private UUID senderAccountId;

    @Column(name = "receiver_account_id", nullable = false)
    private UUID receiverAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private TransactionStatus status;

    @Column(name = "failure_code", length = 80)
    private String failureCode;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public TransactionEntity() {}

    public TransactionEntity(UUID id, String idempotencyKey, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String currency, TransactionStatus status, String failureCode, String failureReason, String correlationId, Long version, OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime completedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount != null ? amount.setScale(4) : null;
        this.currency = currency;
        this.status = status;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.correlationId = correlationId;
        this.version = version != null ? version : 0L;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (amount != null) {
            amount = amount.setScale(4);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
        if (amount != null) {
            amount = amount.setScale(4);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public UUID getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(UUID senderAccountId) { this.senderAccountId = senderAccountId; }

    public UUID getReceiverAccountId() { return receiverAccountId; }
    public void setReceiverAccountId(UUID receiverAccountId) { this.receiverAccountId = receiverAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount.setScale(4) : null; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String idempotencyKey;
        private UUID senderAccountId;
        private UUID receiverAccountId;
        private BigDecimal amount;
        private String currency;
        private TransactionStatus status;
        private String failureCode;
        private String failureReason;
        private String correlationId;
        private Long version = 0L;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private OffsetDateTime completedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder senderAccountId(UUID senderAccountId) { this.senderAccountId = senderAccountId; return this; }
        public Builder receiverAccountId(UUID receiverAccountId) { this.receiverAccountId = receiverAccountId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder status(TransactionStatus status) { this.status = status; return this; }
        public Builder failureCode(String failureCode) { this.failureCode = failureCode; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }

        public TransactionEntity build() {
            return new TransactionEntity(id, idempotencyKey, senderAccountId, receiverAccountId, amount, currency, status, failureCode, failureReason, correlationId, version, createdAt, updatedAt, completedAt);
        }
    }
}
