package com.atomicpay.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private Object error;
    private OffsetDateTime timestamp;
    private String requestId;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, Object error, OffsetDateTime timestamp, String requestId) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now(), requestId != null ? requestId : UUID.randomUUID().toString());
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public Object getError() { return error; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public String getRequestId() { return requestId; }
}
