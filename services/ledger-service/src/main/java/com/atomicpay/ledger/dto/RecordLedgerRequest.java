package com.atomicpay.ledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class RecordLedgerRequest {

    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotNull(message = "Sender account ID is required")
    private UUID senderAccountId;

    @NotNull(message = "Receiver account ID is required")
    private UUID receiverAccountId;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.0001", message = "Transfer amount must be positive")
    private BigDecimal amount;

    private String currency = "INR";

    public RecordLedgerRequest() {}

    public RecordLedgerRequest(UUID transactionId, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount != null ? amount.setScale(4) : null;
        this.currency = currency != null ? currency : "INR";
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(UUID senderAccountId) { this.senderAccountId = senderAccountId; }

    public UUID getReceiverAccountId() { return receiverAccountId; }
    public void setReceiverAccountId(UUID receiverAccountId) { this.receiverAccountId = receiverAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount.setScale(4) : null; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID transactionId;
        private UUID senderAccountId;
        private UUID receiverAccountId;
        private BigDecimal amount;
        private String currency = "INR";

        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder senderAccountId(UUID senderAccountId) { this.senderAccountId = senderAccountId; return this; }
        public Builder receiverAccountId(UUID receiverAccountId) { this.receiverAccountId = receiverAccountId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }

        public RecordLedgerRequest build() {
            return new RecordLedgerRequest(transactionId, senderAccountId, receiverAccountId, amount, currency);
        }
    }
}
