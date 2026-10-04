package com.atomicpay.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_issues", schema = "transactions")
public class ReconciliationIssueEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "issue_type", nullable = false, length = 80)
    private String issueType;

    @Column(name = "expected_value", precision = 19, scale = 4)
    private BigDecimal expectedValue;

    @Column(name = "actual_value", precision = 19, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    public ReconciliationIssueEntity() {}

    public ReconciliationIssueEntity(UUID id, UUID runId, UUID transactionId, UUID accountId, String issueType, BigDecimal expectedValue, BigDecimal actualValue, String status, String resolution, OffsetDateTime createdAt, OffsetDateTime resolvedAt) {
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
    public void setId(UUID id) { this.id = id; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public BigDecimal getExpectedValue() { return expectedValue; }
    public void setExpectedValue(BigDecimal expectedValue) { this.expectedValue = expectedValue; }

    public BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "OPEN";
    }

    public static class Builder {
        private UUID id;
        private UUID runId;
        private UUID transactionId;
        private UUID accountId;
        private String issueType;
        private BigDecimal expectedValue;
        private BigDecimal actualValue;
        private String status = "OPEN";
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

        public ReconciliationIssueEntity build() {
            return new ReconciliationIssueEntity(id, runId, transactionId, accountId, issueType, expectedValue, actualValue, status, resolution, createdAt, resolvedAt);
        }
    }
}
