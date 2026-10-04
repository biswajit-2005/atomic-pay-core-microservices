package com.atomicpay.transaction.entity;

import com.atomicpay.transaction.domain.StepStatus;
import com.atomicpay.transaction.domain.StepType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_steps", schema = "transactions",
       uniqueConstraints = @UniqueConstraint(name = "uq_step_order", columnNames = {"transaction_id", "step_order"}))
public class TransactionStepEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 50)
    private StepType stepType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StepStatus status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public TransactionStepEntity() {}

    public TransactionStepEntity(UUID id, UUID transactionId, Integer stepOrder, StepType stepType, StepStatus status, Integer attempts, OffsetDateTime startedAt, OffsetDateTime completedAt, String errorCode, String errorMessage, OffsetDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
        this.status = status;
        this.attempts = attempts != null ? attempts : 0;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
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

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public StepType getStepType() { return stepType; }
    public void setStepType(StepType stepType) { this.stepType = stepType; }

    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID transactionId;
        private Integer stepOrder;
        private StepType stepType;
        private StepStatus status;
        private Integer attempts = 0;
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private String errorCode;
        private String errorMessage;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder stepOrder(Integer stepOrder) { this.stepOrder = stepOrder; return this; }
        public Builder stepType(StepType stepType) { this.stepType = stepType; return this; }
        public Builder status(StepStatus status) { this.status = status; return this; }
        public Builder attempts(Integer attempts) { this.attempts = attempts; return this; }
        public Builder startedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TransactionStepEntity build() {
            return new TransactionStepEntity(id, transactionId, stepOrder, stepType, status, attempts, startedAt, completedAt, errorCode, errorMessage, createdAt);
        }
    }
}
