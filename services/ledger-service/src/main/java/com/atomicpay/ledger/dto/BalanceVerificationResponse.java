package com.atomicpay.ledger.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceVerificationResponse {
    private UUID accountId;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal netCalculatedBalance;
    private boolean balanced;

    public BalanceVerificationResponse() {}

    public BalanceVerificationResponse(UUID accountId, BigDecimal totalDebits, BigDecimal totalCredits, BigDecimal netCalculatedBalance, boolean balanced) {
        this.accountId = accountId;
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;
        this.netCalculatedBalance = netCalculatedBalance;
        this.balanced = balanced;
    }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public BigDecimal getTotalDebits() { return totalDebits; }
    public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }

    public BigDecimal getTotalCredits() { return totalCredits; }
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }

    public BigDecimal getNetCalculatedBalance() { return netCalculatedBalance; }
    public void setNetCalculatedBalance(BigDecimal netCalculatedBalance) { this.netCalculatedBalance = netCalculatedBalance; }

    public boolean isBalanced() { return balanced; }
    public void setBalanced(boolean balanced) { this.balanced = balanced; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID accountId;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
        private BigDecimal netCalculatedBalance;
        private boolean balanced;

        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder totalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; return this; }
        public Builder totalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; return this; }
        public Builder netCalculatedBalance(BigDecimal netCalculatedBalance) { this.netCalculatedBalance = netCalculatedBalance; return this; }
        public Builder balanced(boolean balanced) { this.balanced = balanced; return this; }

        public BalanceVerificationResponse build() {
            return new BalanceVerificationResponse(accountId, totalDebits, totalCredits, netCalculatedBalance, balanced);
        }
    }
}
