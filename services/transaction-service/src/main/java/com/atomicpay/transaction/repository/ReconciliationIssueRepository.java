package com.atomicpay.transaction.repository;

import com.atomicpay.transaction.entity.ReconciliationIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationIssueRepository extends JpaRepository<ReconciliationIssueEntity, UUID> {
    List<ReconciliationIssueEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<ReconciliationIssueEntity> findAllByOrderByCreatedAtDesc();
    List<ReconciliationIssueEntity> findByRunId(UUID runId);
}
