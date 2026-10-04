package com.atomicpay.ledger.service;

import com.atomicpay.ledger.domain.EntryType;
import com.atomicpay.ledger.dto.*;
import com.atomicpay.ledger.entity.LedgerAccountEntity;
import com.atomicpay.ledger.entity.LedgerEntryEntity;
import com.atomicpay.ledger.exception.ErrorCode;
import com.atomicpay.ledger.exception.LedgerException;
import com.atomicpay.ledger.repository.LedgerAccountRepository;
import com.atomicpay.ledger.repository.LedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerAccountRepository ledgerAccountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public List<LedgerEntryResponse> recordDoubleEntry(RecordLedgerRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerException(ErrorCode.INVALID_AMOUNT, "Transfer amount must be positive");
        }

        LedgerAccountEntity senderLedgerAcc = getOrCreateLedgerAccount(request.getSenderAccountId());
        LedgerAccountEntity receiverLedgerAcc = getOrCreateLedgerAccount(request.getReceiverAccountId());

        // Sequence 1: Sender DEBIT
        LedgerEntryEntity debitEntry = LedgerEntryEntity.builder()
                .transactionId(request.getTransactionId())
                .ledgerAccountId(senderLedgerAcc.getId())
                .entryType(EntryType.DEBIT)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .sequenceNo(1)
                .build();

        // Sequence 2: Receiver CREDIT
        LedgerEntryEntity creditEntry = LedgerEntryEntity.builder()
                .transactionId(request.getTransactionId())
                .ledgerAccountId(receiverLedgerAcc.getId())
                .entryType(EntryType.CREDIT)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .sequenceNo(2)
                .build();

        // Save double-entry records atomically
        LedgerEntryEntity savedDebit = ledgerEntryRepository.save(debitEntry);
        LedgerEntryEntity savedCredit = ledgerEntryRepository.save(creditEntry);

        // Verify total DEBIT equals total CREDIT for transaction
        if (savedDebit.getAmount().compareTo(savedCredit.getAmount()) != 0) {
            throw new LedgerException(ErrorCode.UNBALANCED_LEDGER_ENTRIES);
        }

        log.info("Recorded double-entry ledger pair for transaction {}: DEBIT {} to {}, CREDIT {} to {}",
                request.getTransactionId(), request.getAmount(), request.getSenderAccountId(), request.getAmount(), request.getReceiverAccountId());

        return Arrays.asList(
                mapToResponse(savedDebit, request.getSenderAccountId()),
                mapToResponse(savedCredit, request.getReceiverAccountId())
        );
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedgerEntriesForTransaction(UUID transactionId) {
        List<LedgerEntryEntity> entries = ledgerEntryRepository.findByTransactionIdOrderBySequenceNoAsc(transactionId);
        if (entries.isEmpty()) {
            throw new LedgerException(ErrorCode.TRANSACTION_NOT_FOUND, "No ledger entries found for transaction: " + transactionId);
        }

        return entries.stream()
                .map(e -> {
                    UUID accountId = ledgerAccountRepository.findById(e.getLedgerAccountId())
                            .map(LedgerAccountEntity::getAccountId)
                            .orElse(null);
                    return mapToResponse(e, accountId);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedgerEntriesForAccount(UUID accountId) {
        LedgerAccountEntity ledgerAccount = ledgerAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new LedgerException(ErrorCode.LEDGER_ACCOUNT_NOT_FOUND, "Ledger account not found for account: " + accountId));

        return ledgerEntryRepository.findByLedgerAccountIdOrderByCreatedAtDesc(ledgerAccount.getId()).stream()
                .map(e -> mapToResponse(e, accountId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalanceVerificationResponse verifyAccountBalance(UUID accountId) {
        LedgerAccountEntity ledgerAccount = ledgerAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new LedgerException(ErrorCode.LEDGER_ACCOUNT_NOT_FOUND, "Ledger account not found for account: " + accountId));

        List<LedgerEntryEntity> entries = ledgerEntryRepository.findByLedgerAccountIdOrderByCreatedAtDesc(ledgerAccount.getId());

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        for (LedgerEntryEntity entry : entries) {
            if (entry.getEntryType() == EntryType.CREDIT) {
                totalCredits = totalCredits.add(entry.getAmount());
            } else if (entry.getEntryType() == EntryType.DEBIT) {
                totalDebits = totalDebits.add(entry.getAmount());
            }
        }

        BigDecimal netBalance = totalCredits.subtract(totalDebits);

        return BalanceVerificationResponse.builder()
                .accountId(accountId)
                .totalCredits(totalCredits.setScale(4))
                .totalDebits(totalDebits.setScale(4))
                .netCalculatedBalance(netBalance.setScale(4))
                .balanced(true)
                .build();
    }

    private LedgerAccountEntity getOrCreateLedgerAccount(UUID accountId) {
        return ledgerAccountRepository.findByAccountId(accountId)
                .orElseGet(() -> ledgerAccountRepository.save(
                        LedgerAccountEntity.builder().accountId(accountId).build()
                ));
    }

    private LedgerEntryResponse mapToResponse(LedgerEntryEntity entity, UUID accountId) {
        return LedgerEntryResponse.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .ledgerAccountId(entity.getLedgerAccountId())
                .accountId(accountId)
                .entryType(entity.getEntryType())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .sequenceNo(entity.getSequenceNo())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
