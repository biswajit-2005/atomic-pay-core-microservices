package com.atomicpay.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceResponse {
    private UUID accountId;
    private String accountNumber;
    private BigDecimal availableBalance;
    private String currency;

    public BalanceResponse() {}

    public BalanceResponse(UUID accountId, String accountNumber, BigDecimal availableBalance, String currency) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.availableBalance = availableBalance;
        this.currency = currency;
    }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID accountId;
        private String accountNumber;
        private BigDecimal availableBalance;
        private String currency;

        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder availableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }

        public BalanceResponse build() {
            return new BalanceResponse(accountId, accountNumber, availableBalance, currency);
        }
    }
}
