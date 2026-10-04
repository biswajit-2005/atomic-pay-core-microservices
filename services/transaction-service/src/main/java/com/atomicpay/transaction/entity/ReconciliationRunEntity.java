package com.atomicpay.transaction.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_runs", schema = "transactions")
public class ReconciliationRunEntity {

    @Id
    private UUID id;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "total_transactions")
    private int totalTransactions;

    @Column(name = "mismatches")
    private int mismatches;

    public ReconciliationRunEntity() {}

    public ReconciliationRunEntity(UUID id, OffsetDateTime startedAt, OffsetDateTime completedAt, String status, int totalTransactions, int mismatches) {
        this.id = id;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.totalTransactions = totalTransactions;
        this.mismatches = mismatches;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public int getMismatches() { return mismatches; }
    public void setMismatches(int mismatches) { this.mismatches = mismatches; }

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (startedAt == null) startedAt = OffsetDateTime.now();
        if (status == null) status = "RUNNING";
    }

    public static class Builder {
        private UUID id;
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private String status = "RUNNING";
        private int totalTransactions = 0;
        private int mismatches = 0;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder startedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder mismatches(int mismatches) { this.mismatches = mismatches; return this; }

        public ReconciliationRunEntity build() {
            return new ReconciliationRunEntity(id, startedAt, completedAt, status, totalTransactions, mismatches);
        }
    }
}
