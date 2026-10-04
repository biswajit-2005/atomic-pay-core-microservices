package com.atomicpay.notification.repository;

import com.atomicpay.notification.entity.ProcessedEventEntity;
import com.atomicpay.notification.entity.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, ProcessedEventId> {
}
