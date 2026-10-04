package com.atomicpay.auth.dto;

import java.util.Map;

public class ApiErrorDetails {
    private String code;
    private String message;
    private Map<String, Object> details;

    public ApiErrorDetails() {}

    public ApiErrorDetails(String code, String message, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String code;
        private String message;
        private Map<String, Object> details;

        public Builder code(String code) { this.code = code; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder details(Map<String, Object> details) { this.details = details; return this; }

        public ApiErrorDetails build() {
            return new ApiErrorDetails(code, message, details);
        }
    }
}
