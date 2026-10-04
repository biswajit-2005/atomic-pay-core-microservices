package com.atomicpay.account.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddBeneficiaryRequest {

    @NotNull(message = "Beneficiary account ID is required")
    private UUID beneficiaryAccountId;

    private String nickname;

    public AddBeneficiaryRequest() {}

    public AddBeneficiaryRequest(UUID beneficiaryAccountId, String nickname) {
        this.beneficiaryAccountId = beneficiaryAccountId;
        this.nickname = nickname;
    }

    public UUID getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID beneficiaryAccountId;
        private String nickname;

        public Builder beneficiaryAccountId(UUID beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }

        public AddBeneficiaryRequest build() {
            return new AddBeneficiaryRequest(beneficiaryAccountId, nickname);
        }
    }
}
