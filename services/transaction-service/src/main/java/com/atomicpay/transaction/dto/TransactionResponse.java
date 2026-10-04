package com.atomicpay.transaction.dto;

import com.atomicpay.transaction.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionResponse {
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime completedAt;
    private List<TransactionStepResponse> steps;

    public TransactionResponse() {}

    public TransactionResponse(UUID id, String idempotencyKey, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String currency, TransactionStatus status, String failureCode, String failureReason, String correlationId, OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime completedAt, List<TransactionStepResponse> steps) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.steps = steps;
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
    public void setAmount(BigDecimal amount) { this.amount = amount; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public List<TransactionStepResponse> getSteps() { return steps; }
    public void setSteps(List<TransactionStepResponse> steps) { this.steps = steps; }

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
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private OffsetDateTime completedAt;
        private List<TransactionStepResponse> steps;

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
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder steps(List<TransactionStepResponse> steps) { this.steps = steps; return this; }

        public TransactionResponse build() {
            return new TransactionResponse(id, idempotencyKey, senderAccountId, receiverAccountId, amount, currency, status, failureCode, failureReason, correlationId, createdAt, updatedAt, completedAt, steps);
        }
    }
}
