package com.atomicpay.ledger.controller;

import com.atomicpay.ledger.dto.*;
import com.atomicpay.ledger.service.LedgerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/ledger/entries")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> recordDoubleEntry(
            @Valid @RequestBody RecordLedgerRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        List<LedgerEntryResponse> response = ledgerService.recordDoubleEntry(request);
        return new ResponseEntity<>(ApiResponse.ok(response, requestId), HttpStatus.CREATED);
    }

    @GetMapping("/transactions/{transactionId}/ledger")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> getTransactionLedger(
            @PathVariable("transactionId") UUID transactionId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        List<LedgerEntryResponse> response = ledgerService.getLedgerEntriesForTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @GetMapping("/accounts/{accountId}/ledger")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> getAccountLedger(
            @PathVariable("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        List<LedgerEntryResponse> response = ledgerService.getLedgerEntriesForAccount(accountId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @GetMapping("/accounts/{accountId}/ledger/verify")
    public ResponseEntity<ApiResponse<BalanceVerificationResponse>> verifyAccountBalance(
            @PathVariable("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        BalanceVerificationResponse response = ledgerService.verifyAccountBalance(accountId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }
}
