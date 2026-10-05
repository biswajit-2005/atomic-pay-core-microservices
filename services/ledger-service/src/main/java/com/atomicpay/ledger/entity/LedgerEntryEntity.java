package com.atomicpay.ledger.entity;

import com.atomicpay.ledger.domain.EntryType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", schema = "ledger",
       uniqueConstraints = @UniqueConstraint(name = "uq_ledger_tx_seq", columnNames = {"transaction_id", "sequence_no"}))
public class LedgerEntryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "ledger_account_id", nullable = false)
    private UUID ledgerAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, columnDefinition = "CHAR(3)", length = 3)
    private String currency;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public LedgerEntryEntity() {}

    public LedgerEntryEntity(UUID id, UUID transactionId, UUID ledgerAccountId, EntryType entryType, BigDecimal amount, String currency, Integer sequenceNo, OffsetDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerAccountId = ledgerAccountId;
        this.entryType = entryType;
        this.amount = amount != null ? amount.setScale(4) : null;
        this.currency = currency;
        this.sequenceNo = sequenceNo;
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
        if (amount != null) {
            amount = amount.setScale(4);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getLedgerAccountId() { return ledgerAccountId; }
    public void setLedgerAccountId(UUID ledgerAccountId) { this.ledgerAccountId = ledgerAccountId; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount.setScale(4) : null; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID transactionId;
        private UUID ledgerAccountId;
        private EntryType entryType;
        private BigDecimal amount;
        private String currency;
        private Integer sequenceNo;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder ledgerAccountId(UUID ledgerAccountId) { this.ledgerAccountId = ledgerAccountId; return this; }
        public Builder entryType(EntryType entryType) { this.entryType = entryType; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder sequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LedgerEntryEntity build() {
            return new LedgerEntryEntity(id, transactionId, ledgerAccountId, entryType, amount, currency, sequenceNo, createdAt);
        }
    }
}
