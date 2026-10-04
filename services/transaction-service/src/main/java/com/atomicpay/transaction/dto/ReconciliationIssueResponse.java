package com.atomicpay.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ReconciliationIssueResponse {

    private UUID id;
    private UUID runId;
    private UUID transactionId;
    private UUID accountId;
    private String issueType;
    private BigDecimal expectedValue;
    private BigDecimal actualValue;
    private String status;
    private String resolution;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;

    public ReconciliationIssueResponse() {}

    public ReconciliationIssueResponse(UUID id, UUID runId, UUID transactionId, UUID accountId, String issueType, BigDecimal expectedValue, BigDecimal actualValue, String status, String resolution, OffsetDateTime createdAt, OffsetDateTime resolvedAt) {
        this.id = id;
        this.runId = runId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.issueType = issueType;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.status = status;
        this.resolution = resolution;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getAccountId() { return accountId; }
    public String getIssueType() { return issueType; }
    public BigDecimal getExpectedValue() { return expectedValue; }
    public BigDecimal getActualValue() { return actualValue; }
    public String getStatus() { return status; }
    public String getResolution() { return resolution; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }

    public static class Builder {
        private UUID id;
        private UUID runId;
        private UUID transactionId;
        private UUID accountId;
        private String issueType;
        private BigDecimal expectedValue;
        private BigDecimal actualValue;
        private String status;
        private String resolution;
        private OffsetDateTime createdAt;
        private OffsetDateTime resolvedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder runId(UUID runId) { this.runId = runId; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder issueType(String issueType) { this.issueType = issueType; return this; }
        public Builder expectedValue(BigDecimal expectedValue) { this.expectedValue = expectedValue; return this; }
        public Builder actualValue(BigDecimal actualValue) { this.actualValue = actualValue; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder resolution(String resolution) { this.resolution = resolution; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder resolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public ReconciliationIssueResponse build() {
            return new ReconciliationIssueResponse(id, runId, transactionId, accountId, issueType, expectedValue, actualValue, status, resolution, createdAt, resolvedAt);
        }
    }
}
