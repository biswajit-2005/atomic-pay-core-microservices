package com.atomicpay.account.dto;

import com.atomicpay.account.domain.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private AccountType accountType = AccountType.WALLET;

    private String currency = "INR";

    private BigDecimal initialDeposit;

    public CreateAccountRequest() {}

    public CreateAccountRequest(UUID userId, AccountType accountType, String currency, BigDecimal initialDeposit) {
        this.userId = userId;
        this.accountType = accountType != null ? accountType : AccountType.WALLET;
        this.currency = currency != null ? currency : "INR";
        this.initialDeposit = initialDeposit;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID userId;
        private AccountType accountType = AccountType.WALLET;
        private String currency = "INR";
        private BigDecimal initialDeposit;

        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder accountType(AccountType accountType) { this.accountType = accountType; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder initialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; return this; }

        public CreateAccountRequest build() {
            return new CreateAccountRequest(userId, accountType, currency, initialDeposit);
        }
    }
}
