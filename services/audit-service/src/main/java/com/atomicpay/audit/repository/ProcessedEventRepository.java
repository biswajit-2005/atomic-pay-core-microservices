package com.atomicpay.audit.repository;

import com.atomicpay.audit.entity.ProcessedEventEntity;
import com.atomicpay.audit.entity.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, ProcessedEventId> {
}
