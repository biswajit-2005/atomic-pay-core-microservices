package com.atomicpay.transaction.repository;

import com.atomicpay.transaction.entity.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, UUID> {
    Optional<IdempotencyRecordEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}
