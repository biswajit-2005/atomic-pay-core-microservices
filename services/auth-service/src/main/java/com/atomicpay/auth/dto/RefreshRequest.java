package com.atomicpay.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshRequest() {}

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String refreshToken;

        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }

        public RefreshRequest build() {
            return new RefreshRequest(refreshToken);
        }
    }
}
