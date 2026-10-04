package com.atomicpay.ledger.repository;

import com.atomicpay.ledger.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    List<LedgerEntryEntity> findByTransactionIdOrderBySequenceNoAsc(UUID transactionId);
    List<LedgerEntryEntity> findByLedgerAccountIdOrderByCreatedAtDesc(UUID ledgerAccountId);
}
