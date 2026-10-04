package com.atomicpay.transaction.client;

import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AccountBalanceService {

    private static final Logger log = LoggerFactory.getLogger(AccountBalanceService.class);

    private final EntityManager entityManager;

    public AccountBalanceService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static class AccountData {
        private final UUID id;
        private final UUID userId;
        private final String accountNumber;
        private final String currency;
        private final String status;
        private final BigDecimal currentBalance;

        public AccountData(UUID id, UUID userId, String accountNumber, String currency, String status, BigDecimal currentBalance) {
            this.id = id;
            this.userId = userId;
            this.accountNumber = accountNumber;
            this.currency = currency;
            this.status = status;
            this.currentBalance = currentBalance;
        }

        public UUID getId() { return id; }
        public UUID getUserId() { return userId; }
        public String getAccountNumber() { return accountNumber; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
    }

    @Transactional
    public AccountData lockAccountForUpdate(UUID accountId) {
        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT id, user_id, account_number, currency, status, current_balance FROM accounts.accounts WHERE id = :id FOR UPDATE")
                .setParameter("id", accountId)
                .getSingleResult();

        if (row == null) {
            throw new TransactionException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + accountId);
        }

        return new AccountData(
                (UUID) row[0],
                (UUID) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (BigDecimal) row[5]
        );
    }

    @Transactional
    public void executeTransfer(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) {
        if (senderAccountId.equals(receiverAccountId)) {
            throw new TransactionException(ErrorCode.SELF_TRANSFER_NOT_ALLOWED, "Self transfer is not allowed");
        }

        // Section 21 Rule: Deterministic ID Order Locking to avoid Deadlocks
        UUID firstLockId = senderAccountId.compareTo(receiverAccountId) < 0 ? senderAccountId : receiverAccountId;
        UUID secondLockId = senderAccountId.compareTo(receiverAccountId) < 0 ? receiverAccountId : senderAccountId;

        log.info("Acquiring pessimistic row locks in deterministic ID order: first={}, second={}", firstLockId, secondLockId);

        AccountData firstAccount = lockAccountForUpdate(firstLockId);
        AccountData secondAccount = lockAccountForUpdate(secondLockId);

        AccountData senderAccount = firstLockId.equals(senderAccountId) ? firstAccount : secondAccount;
        AccountData receiverAccount = firstLockId.equals(receiverAccountId) ? firstAccount : secondAccount;

        if (!"ACTIVE".equalsIgnoreCase(senderAccount.getStatus())) {
            throw new TransactionException(ErrorCode.ACCOUNT_SUSPENDED, "Sender account is not active");
        }
        if (!"ACTIVE".equalsIgnoreCase(receiverAccount.getStatus())) {
            throw new TransactionException(ErrorCode.ACCOUNT_SUSPENDED, "Receiver account is not active");
        }

        if (senderAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new TransactionException(
                    ErrorCode.INSUFFICIENT_BALANCE,
                    String.format("Insufficient balance in sender account. Available: %s, Requested: %s", senderAccount.getCurrentBalance(), amount)
            );
        }

        // Update sender balance
        int debitedRows = entityManager.createNativeQuery(
                        "UPDATE accounts.accounts SET current_balance = current_balance - :amount, version = version + 1, updated_at = :now WHERE id = :id AND current_balance >= :amount")
                .setParameter("amount", amount)
                .setParameter("now", Timestamp.from(OffsetDateTime.now().toInstant()))
                .setParameter("id", senderAccountId)
                .executeUpdate();

        if (debitedRows != 1) {
            throw new TransactionException(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient balance during debit execution");
        }

        // Update receiver balance
        int creditedRows = entityManager.createNativeQuery(
                        "UPDATE accounts.accounts SET current_balance = current_balance + :amount, version = version + 1, updated_at = :now WHERE id = :id")
                .setParameter("amount", amount)
                .setParameter("now", Timestamp.from(OffsetDateTime.now().toInstant()))
                .setParameter("id", receiverAccountId)
                .executeUpdate();

        if (creditedRows != 1) {
            throw new TransactionException(ErrorCode.INTERNAL_ERROR, "Failed to credit receiver account balance");
        }

        log.info("Successfully debited {} from sender {} and credited to receiver {}", amount, senderAccountId, receiverAccountId);
    }
}
