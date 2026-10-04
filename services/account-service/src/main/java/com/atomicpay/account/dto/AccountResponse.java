package com.atomicpay.account.dto;

import com.atomicpay.account.domain.AccountStatus;
import com.atomicpay.account.domain.AccountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountResponse {
    private UUID id;
    private UUID userId;
    private String accountNumber;
    private String currency;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal currentBalance;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public AccountResponse() {}

    public AccountResponse(UUID id, UUID userId, String accountNumber, String currency, AccountType accountType, AccountStatus status, BigDecimal currentBalance, Long version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.accountType = accountType;
        this.status = status;
        this.currentBalance = currentBalance;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String accountNumber;
        private String currency;
        private AccountType accountType;
        private AccountStatus status;
        private BigDecimal currentBalance;
        private Long version;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder accountType(AccountType accountType) { this.accountType = accountType; return this; }
        public Builder status(AccountStatus status) { this.status = status; return this; }
        public Builder currentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AccountResponse build() {
            return new AccountResponse(id, userId, accountNumber, currency, accountType, status, currentBalance, version, createdAt, updatedAt);
        }
    }
}
