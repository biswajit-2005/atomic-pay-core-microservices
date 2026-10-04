package com.atomicpay.audit.repository;

import com.atomicpay.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
    List<AuditLogEntity> findByTransactionIdOrderByCreatedAtDesc(UUID transactionId);
    List<AuditLogEntity> findByActorUserIdOrderByCreatedAtDesc(UUID actorUserId);
    List<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
