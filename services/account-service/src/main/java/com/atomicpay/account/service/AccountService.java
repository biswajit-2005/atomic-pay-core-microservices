package com.atomicpay.account.service;

import com.atomicpay.account.domain.AccountStatus;
import com.atomicpay.account.domain.AccountType;
import com.atomicpay.account.dto.*;
import com.atomicpay.account.entity.AccountEntity;
import com.atomicpay.account.entity.BeneficiaryEntity;
import com.atomicpay.account.exception.AccountException;
import com.atomicpay.account.exception.ErrorCode;
import com.atomicpay.account.repository.AccountRepository;
import com.atomicpay.account.repository.BeneficiaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    public AccountService(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request.getCurrency() != null && !"INR".equalsIgnoreCase(request.getCurrency())) {
            throw new AccountException(ErrorCode.CURRENCY_NOT_SUPPORTED, "Only INR currency is supported for MVP");
        }

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountException(ErrorCode.INVALID_AMOUNT, "Initial deposit cannot be negative");
        }

        String accountNumber = generateUniqueAccountNumber();

        AccountEntity account = AccountEntity.builder()
                .userId(request.getUserId())
                .accountNumber(accountNumber)
                .currency("INR")
                .accountType(request.getAccountType() != null ? request.getAccountType() : AccountType.WALLET)
                .status(AccountStatus.ACTIVE)
                .currentBalance(initialDeposit)
                .build();

        AccountEntity savedAccount = accountRepository.save(account);
        log.info("Created new account {} for user {}", savedAccount.getAccountNumber(), savedAccount.getUserId());

        return mapToAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found with ID: " + accountId));
        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found with number: " + accountNumber));
        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found with ID: " + accountId));

        return BalanceResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .availableBalance(account.getCurrentBalance())
                .currency(account.getCurrency())
                .build();
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(UUID ownerUserId, AddBeneficiaryRequest request) {
        AccountEntity beneficiaryAccount = accountRepository.findById(request.getBeneficiaryAccountId())
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND, "Beneficiary account not found"));

        if (ownerUserId.equals(beneficiaryAccount.getUserId())) {
            throw new AccountException(ErrorCode.INVALID_REQUEST, "Cannot add your own account as a beneficiary");
        }

        if (beneficiaryRepository.existsByOwnerUserIdAndBeneficiaryAccountId(ownerUserId, request.getBeneficiaryAccountId())) {
            throw new AccountException(ErrorCode.BENEFICIARY_ALREADY_EXISTS, "Beneficiary account already added");
        }

        BeneficiaryEntity beneficiary = BeneficiaryEntity.builder()
                .ownerUserId(ownerUserId)
                .beneficiaryAccountId(request.getBeneficiaryAccountId())
                .nickname(request.getNickname())
                .build();

        BeneficiaryEntity saved = beneficiaryRepository.save(beneficiary);
        log.info("Added beneficiary account {} for user {}", beneficiaryAccount.getAccountNumber(), ownerUserId);

        return BeneficiaryResponse.builder()
                .id(saved.getId())
                .ownerUserId(saved.getOwnerUserId())
                .beneficiaryAccountId(saved.getBeneficiaryAccountId())
                .beneficiaryAccountNumber(beneficiaryAccount.getAccountNumber())
                .nickname(saved.getNickname())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiaries(UUID ownerUserId) {
        return beneficiaryRepository.findByOwnerUserId(ownerUserId).stream()
                .map(b -> {
                    String accNum = accountRepository.findById(b.getBeneficiaryAccountId())
                            .map(AccountEntity::getAccountNumber)
                            .orElse("UNKNOWN");
                    return BeneficiaryResponse.builder()
                            .id(b.getId())
                            .ownerUserId(b.getOwnerUserId())
                            .beneficiaryAccountId(b.getBeneficiaryAccountId())
                            .beneficiaryAccountNumber(accNum)
                            .nickname(b.getNickname())
                            .createdAt(b.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long) (RANDOM.nextDouble() * 9000000000L);
            accountNumber = "ACC" + number;
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse mapToAccountResponse(AccountEntity account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .currentBalance(account.getCurrentBalance())
                .version(account.getVersion())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
