package com.atomicpay.account.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries", schema = "accounts")
public class BeneficiaryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "beneficiary_account_id", nullable = false)
    private UUID beneficiaryAccountId;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public BeneficiaryEntity() {}

    public BeneficiaryEntity(UUID id, UUID ownerUserId, UUID beneficiaryAccountId, String nickname, OffsetDateTime createdAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.beneficiaryAccountId = beneficiaryAccountId;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }

    public UUID getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID ownerUserId;
        private UUID beneficiaryAccountId;
        private String nickname;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder ownerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; return this; }
        public Builder beneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public BeneficiaryEntity build() {
            return new BeneficiaryEntity(id, ownerUserId, beneficiaryAccountId, nickname, createdAt);
        }
    }
}
