package com.atomicpay.account.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BeneficiaryResponse {
    private UUID id;
    private UUID ownerUserId;
    private UUID beneficiaryAccountId;
    private String beneficiaryAccountNumber;
    private String nickname;
    private OffsetDateTime createdAt;

    public BeneficiaryResponse() {}

    public BeneficiaryResponse(UUID id, UUID ownerUserId, UUID beneficiaryAccountId, String beneficiaryAccountNumber, String nickname, OffsetDateTime createdAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.beneficiaryAccountId = beneficiaryAccountId;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }

    public UUID getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; }

    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID ownerUserId;
        private UUID beneficiaryAccountId;
        private String beneficiaryAccountNumber;
        private String nickname;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder ownerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; return this; }
        public Builder beneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; return this; }
        public Builder beneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public BeneficiaryResponse build() {
            return new BeneficiaryResponse(id, ownerUserId, beneficiaryAccountId, beneficiaryAccountNumber, nickname, createdAt);
        }
    }
}
