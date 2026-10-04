package com.atomicpay.transaction.repository;

import com.atomicpay.transaction.domain.TransactionStatus;
import com.atomicpay.transaction.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findBySenderAccountIdAndIdempotencyKey(UUID senderAccountId, String idempotencyKey);
    List<TransactionEntity> findBySenderAccountIdOrReceiverAccountIdOrderByCreatedAtDesc(UUID senderAccountId, UUID receiverAccountId);
    List<TransactionEntity> findByStatus(TransactionStatus status);
}
