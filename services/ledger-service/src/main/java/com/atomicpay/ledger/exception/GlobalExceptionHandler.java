package com.atomicpay.ledger.exception;

import com.atomicpay.ledger.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LedgerException.class)
    public ResponseEntity<ApiResponse<Void>> handleLedgerException(LedgerException ex, HttpServletRequest request) {
        String requestId = getCorrelationOrRequestId(request);
        log.warn("LedgerException [requestId={}]: {} - {}", requestId, ex.getErrorCode(), ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getDetails(),
                requestId
        );
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String requestId = getCorrelationOrRequestId(request);
        Map<String, Object> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation error [requestId={}]: {}", requestId, fieldErrors);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST.name(),
                "Validation failed for request parameters",
                fieldErrors,
                requestId
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String requestId = getCorrelationOrRequestId(request);
        log.error("Unhandled internal error [requestId={}]: ", requestId, ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected internal error occurred",
                null,
                requestId
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCorrelationOrRequestId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }
        return UUID.randomUUID().toString();
    }
}
