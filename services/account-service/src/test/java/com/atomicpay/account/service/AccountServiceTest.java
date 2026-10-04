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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;
    private UUID accountId;
    private AccountEntity sampleAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        sampleAccount = AccountEntity.builder()
                .id(accountId)
                .userId(userId)
                .accountNumber("ACC1234567890")
                .currency("INR")
                .accountType(AccountType.WALLET)
                .status(AccountStatus.ACTIVE)
                .currentBalance(new BigDecimal("10000.0000"))
                .version(0L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create account with INR currency and initial deposit")
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .currency("INR")
                .accountType(AccountType.WALLET)
                .initialDeposit(new BigDecimal("5000.0000"))
                .build();

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(sampleAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("ACC1234567890", response.getAccountNumber());
        assertEquals("INR", response.getCurrency());
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should throw CURRENCY_NOT_SUPPORTED for non-INR currency")
    void createAccount_UnsupportedCurrency_ThrowsException() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .currency("USD")
                .build();

        AccountException ex = assertThrows(AccountException.class, () -> accountService.createAccount(request));
        assertEquals(ErrorCode.CURRENCY_NOT_SUPPORTED, ex.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw INVALID_AMOUNT for negative initial deposit")
    void createAccount_NegativeDeposit_ThrowsException() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .currency("INR")
                .initialDeposit(new BigDecimal("-100.0000"))
                .build();

        AccountException ex = assertThrows(AccountException.class, () -> accountService.createAccount(request));
        assertEquals(ErrorCode.INVALID_AMOUNT, ex.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return available balance for valid account ID")
    void getBalance_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(sampleAccount));

        BalanceResponse response = accountService.getBalance(accountId);

        assertNotNull(response);
        assertEquals(accountId, response.getAccountId());
        assertEquals(new BigDecimal("10000.0000"), response.getAvailableBalance());
        assertEquals("INR", response.getCurrency());
    }

    @Test
    @DisplayName("Should throw ACCOUNT_NOT_FOUND when getting balance for non-existent account")
    void getBalance_AccountNotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        AccountException ex = assertThrows(AccountException.class, () -> accountService.getBalance(nonExistentId));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should throw INVALID_REQUEST when adding own account as beneficiary")
    void addBeneficiary_SelfBeneficiary_ThrowsException() {
        AddBeneficiaryRequest request = AddBeneficiaryRequest.builder()
                .beneficiaryAccountId(accountId)
                .nickname("My Other Account")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(sampleAccount));

        AccountException ex = assertThrows(AccountException.class, () -> accountService.addBeneficiary(userId, request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verify(beneficiaryRepository, never()).save(any());
    }
}
