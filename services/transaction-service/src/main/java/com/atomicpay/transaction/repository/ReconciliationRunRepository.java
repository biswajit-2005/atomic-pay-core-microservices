package com.atomicpay.transaction.repository;

import com.atomicpay.transaction.entity.ReconciliationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRunEntity, UUID> {
    List<ReconciliationRunEntity> findAllByOrderByStartedAtDesc();
}
