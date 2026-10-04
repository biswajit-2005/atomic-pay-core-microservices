package com.atomicpay.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiErrorDetails error;
    private OffsetDateTime timestamp;
    private String requestId;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, ApiErrorDetails error, OffsetDateTime timestamp, String requestId) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public ApiErrorDetails getError() { return error; }
    public void setError(ApiErrorDetails error) { this.error = error; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(
                true,
                data,
                null,
                OffsetDateTime.now(),
                requestId != null ? requestId : UUID.randomUUID().toString()
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String code, String message, Object details, String requestId) {
        Map<String, Object> detailsMap = details instanceof Map ? (Map<String, Object>) details : Collections.emptyMap();
        ApiErrorDetails errorDetails = ApiErrorDetails.builder()
                .code(code)
                .message(message)
                .details(detailsMap)
                .build();

        return new ApiResponse<>(
                false,
                null,
                errorDetails,
                OffsetDateTime.now(),
                requestId != null ? requestId : UUID.randomUUID().toString()
        );
    }
}
