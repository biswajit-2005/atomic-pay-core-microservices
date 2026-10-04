package com.atomicpay.ledger.service;

import com.atomicpay.ledger.domain.EntryType;
import com.atomicpay.ledger.dto.*;
import com.atomicpay.ledger.entity.LedgerAccountEntity;
import com.atomicpay.ledger.entity.LedgerEntryEntity;
import com.atomicpay.ledger.exception.ErrorCode;
import com.atomicpay.ledger.exception.LedgerException;
import com.atomicpay.ledger.repository.LedgerAccountRepository;
import com.atomicpay.ledger.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private LedgerAccountEntity senderLedgerAcc;
    private LedgerAccountEntity receiverLedgerAcc;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        senderAccountId = UUID.randomUUID();
        receiverAccountId = UUID.randomUUID();

        senderLedgerAcc = LedgerAccountEntity.builder()
                .id(UUID.randomUUID())
                .accountId(senderAccountId)
                .createdAt(OffsetDateTime.now())
                .build();

        receiverLedgerAcc = LedgerAccountEntity.builder()
                .id(UUID.randomUUID())
                .accountId(receiverAccountId)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully record balanced double-entry DEBIT and CREDIT pair")
    void recordDoubleEntry_Success() {
        RecordLedgerRequest request = RecordLedgerRequest.builder()
                .transactionId(transactionId)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(new BigDecimal("1500.0000"))
                .currency("INR")
                .build();

        when(ledgerAccountRepository.findByAccountId(senderAccountId)).thenReturn(Optional.of(senderLedgerAcc));
        when(ledgerAccountRepository.findByAccountId(receiverAccountId)).thenReturn(Optional.of(receiverLedgerAcc));
        when(ledgerEntryRepository.save(any(LedgerEntryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<LedgerEntryResponse> entries = ledgerService.recordDoubleEntry(request);

        assertNotNull(entries);
        assertEquals(2, entries.size());

        LedgerEntryResponse debit = entries.get(0);
        assertEquals(EntryType.DEBIT, debit.getEntryType());
        assertEquals(new BigDecimal("1500.0000"), debit.getAmount());

        LedgerEntryResponse credit = entries.get(1);
        assertEquals(EntryType.CREDIT, credit.getEntryType());
        assertEquals(new BigDecimal("1500.0000"), credit.getAmount());

        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntryEntity.class));
    }

    @Test
    @DisplayName("Should throw INVALID_AMOUNT when recording non-positive amount")
    void recordDoubleEntry_ZeroAmount_ThrowsException() {
        RecordLedgerRequest request = RecordLedgerRequest.builder()
                .transactionId(transactionId)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(BigDecimal.ZERO)
                .build();

        LedgerException ex = assertThrows(LedgerException.class, () -> ledgerService.recordDoubleEntry(request));
        assertEquals(ErrorCode.INVALID_AMOUNT, ex.getErrorCode());
        verify(ledgerEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should verify net account balance correctly from append-only ledger entries")
    void verifyAccountBalance_Success() {
        LedgerEntryEntity credit1 = LedgerEntryEntity.builder()
                .ledgerAccountId(receiverLedgerAcc.getId())
                .entryType(EntryType.CREDIT)
                .amount(new BigDecimal("5000.0000"))
                .build();

        LedgerEntryEntity debit1 = LedgerEntryEntity.builder()
                .ledgerAccountId(receiverLedgerAcc.getId())
                .entryType(EntryType.DEBIT)
                .amount(new BigDecimal("1200.0000"))
                .build();

        when(ledgerAccountRepository.findByAccountId(receiverAccountId)).thenReturn(Optional.of(receiverLedgerAcc));
        when(ledgerEntryRepository.findByLedgerAccountIdOrderByCreatedAtDesc(receiverLedgerAcc.getId()))
                .thenReturn(Arrays.asList(credit1, debit1));

        BalanceVerificationResponse response = ledgerService.verifyAccountBalance(receiverAccountId);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.0000"), response.getTotalCredits());
        assertEquals(new BigDecimal("1200.0000"), response.getTotalDebits());
        assertEquals(new BigDecimal("3800.0000"), response.getNetCalculatedBalance());
        assertTrue(response.isBalanced());
    }
}
