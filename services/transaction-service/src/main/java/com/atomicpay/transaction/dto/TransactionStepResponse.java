package com.atomicpay.transaction.dto;

import com.atomicpay.transaction.domain.StepStatus;
import com.atomicpay.transaction.domain.StepType;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TransactionStepResponse {
    private UUID id;
    private Integer stepOrder;
    private StepType stepType;
    private StepStatus status;
    private Integer attempts;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private String errorCode;
    private String errorMessage;

    public TransactionStepResponse() {}

    public TransactionStepResponse(UUID id, Integer stepOrder, StepType stepType, StepStatus status, Integer attempts, OffsetDateTime startedAt, OffsetDateTime completedAt, String errorCode, String errorMessage) {
        this.id = id;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
        this.status = status;
        this.attempts = attempts;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private Integer stepOrder;
        private StepType stepType;
        private StepStatus status;
        private Integer attempts;
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private String errorCode;
        private String errorMessage;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder stepOrder(Integer stepOrder) { this.stepOrder = stepOrder; return this; }
        public Builder stepType(StepType stepType) { this.stepType = stepType; return this; }
        public Builder status(StepStatus status) { this.status = status; return this; }
        public Builder attempts(Integer attempts) { this.attempts = attempts; return this; }
        public Builder startedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public TransactionStepResponse build() {
            return new TransactionStepResponse(id, stepOrder, stepType, status, attempts, startedAt, completedAt, errorCode, errorMessage);
        }
    }
}
