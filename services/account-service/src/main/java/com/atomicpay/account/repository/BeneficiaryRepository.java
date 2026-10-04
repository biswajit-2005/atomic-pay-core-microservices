package com.atomicpay.account.repository;

import com.atomicpay.account.entity.BeneficiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, UUID> {
    List<BeneficiaryEntity> findByOwnerUserId(UUID ownerUserId);
    Optional<BeneficiaryEntity> findByOwnerUserIdAndBeneficiaryAccountId(UUID ownerUserId, UUID beneficiaryAccountId);
    boolean existsByOwnerUserIdAndBeneficiaryAccountId(UUID ownerUserId, UUID beneficiaryAccountId);
}
