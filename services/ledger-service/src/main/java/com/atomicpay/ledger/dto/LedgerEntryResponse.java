package com.atomicpay.ledger.dto;

import com.atomicpay.ledger.domain.EntryType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class LedgerEntryResponse {
    private UUID id;
    private UUID transactionId;
    private UUID ledgerAccountId;
    private UUID accountId;
    private EntryType entryType;
    private BigDecimal amount;
    private String currency;
    private Integer sequenceNo;
    private OffsetDateTime createdAt;

    public LedgerEntryResponse() {}

    public LedgerEntryResponse(UUID id, UUID transactionId, UUID ledgerAccountId, UUID accountId, EntryType entryType, BigDecimal amount, String currency, Integer sequenceNo, OffsetDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerAccountId = ledgerAccountId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
        this.sequenceNo = sequenceNo;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getLedgerAccountId() { return ledgerAccountId; }
    public void setLedgerAccountId(UUID ledgerAccountId) { this.ledgerAccountId = ledgerAccountId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

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
        private UUID accountId;
        private EntryType entryType;
        private BigDecimal amount;
        private String currency;
        private Integer sequenceNo;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder ledgerAccountId(UUID ledgerAccountId) { this.ledgerAccountId = ledgerAccountId; return this; }
        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder entryType(EntryType entryType) { this.entryType = entryType; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder sequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LedgerEntryResponse build() {
            return new LedgerEntryResponse(id, transactionId, ledgerAccountId, accountId, entryType, amount, currency, sequenceNo, createdAt);
        }
    }
}
