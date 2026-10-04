package com.atomicpay.auth.repository;

import com.atomicpay.auth.entity.KycProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfileEntity, UUID> {
    Optional<KycProfileEntity> findByUserId(UUID userId);
}
