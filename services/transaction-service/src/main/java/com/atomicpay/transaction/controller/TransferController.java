package com.atomicpay.transaction.controller;

import com.atomicpay.transaction.dto.*;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransfer(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId,
            @RequestHeader(name = "X-Simulate-Failure", required = false) String simulateFailure,
            @RequestParam(name = "userId", required = false) UUID paramUserId,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpServletRequest
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new TransactionException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        UUID targetUserId = paramUserId;
        if (targetUserId == null && xUserId != null && !xUserId.isBlank()) {
            targetUserId = UUID.fromString(xUserId);
        }
        if (targetUserId == null) {
            targetUserId = request.getSenderAccountId(); // Fallback to sender account ID if user ID not specified
        }

        String correlationId = getCorrelationId(httpServletRequest);
        TransactionResponse response = transferService.processTransfer(idempotencyKey, targetUserId, request, correlationId, simulateFailure);
        return new ResponseEntity<>(ApiResponse.ok(response, correlationId), HttpStatus.OK);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable("transactionId") UUID transactionId,
            HttpServletRequest httpServletRequest
    ) {
        String correlationId = getCorrelationId(httpServletRequest);
        TransactionResponse response = transferService.getTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.ok(response, correlationId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsForAccount(
            @RequestParam("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String correlationId = getCorrelationId(httpServletRequest);
        List<TransactionResponse> response = transferService.getTransactionsForAccount(accountId);
        return ResponseEntity.ok(ApiResponse.ok(response, correlationId));
    }

    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = request.getHeader("X-Request-ID");
        }
        return correlationId != null && !correlationId.isBlank() ? correlationId : UUID.randomUUID().toString();
    }
}
