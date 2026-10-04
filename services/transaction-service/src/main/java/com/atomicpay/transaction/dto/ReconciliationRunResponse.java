package com.atomicpay.transaction.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReconciliationRunResponse {

    private UUID id;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private String status;
    private int totalTransactions;
    private int mismatches;

    public ReconciliationRunResponse() {}

    public ReconciliationRunResponse(UUID id, OffsetDateTime startedAt, OffsetDateTime completedAt, String status, int totalTransactions, int mismatches) {
        this.id = id;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.totalTransactions = totalTransactions;
        this.mismatches = mismatches;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public String getStatus() { return status; }
    public int getTotalTransactions() { return totalTransactions; }
    public int getMismatches() { return mismatches; }

    public static class Builder {
        private UUID id;
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private String status;
        private int totalTransactions;
        private int mismatches;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder startedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder mismatches(int mismatches) { this.mismatches = mismatches; return this; }

        public ReconciliationRunResponse build() {
            return new ReconciliationRunResponse(id, startedAt, completedAt, status, totalTransactions, mismatches);
        }
    }
}
