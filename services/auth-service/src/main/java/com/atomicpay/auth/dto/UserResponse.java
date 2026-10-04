package com.atomicpay.auth.dto;

import com.atomicpay.auth.domain.KycStatus;
import com.atomicpay.auth.domain.UserRole;
import com.atomicpay.auth.domain.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private KycStatus kycStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UserResponse() {}

    public UserResponse(UUID id, String email, String fullName, String phone, UserRole role, UserStatus status, KycStatus kycStatus, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String email;
        private String fullName;
        private String phone;
        private UserRole role;
        private UserStatus status;
        private KycStatus kycStatus;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder kycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserResponse build() {
            return new UserResponse(id, email, fullName, phone, role, status, kycStatus, createdAt, updatedAt);
        }
    }
}
