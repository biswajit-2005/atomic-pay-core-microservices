package com.atomicpay.account.controller;

import com.atomicpay.account.dto.*;
import com.atomicpay.account.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(ApiResponse.ok(response, requestId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts(
            @RequestParam(name = "userId", required = false) UUID userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        UUID targetUserId = userId;
        if (targetUserId == null && xUserId != null && !xUserId.isBlank()) {
            targetUserId = UUID.fromString(xUserId);
        }
        if (targetUserId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", "userId parameter or X-User-Id header is required", null, requestId));
        }

        List<AccountResponse> response = accountService.getAccountsByUserId(targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        AccountResponse response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @PathVariable("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        BalanceResponse response = accountService.getBalance(accountId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @PostMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @RequestHeader(name = "X-User-Id", required = false) String xUserId,
            @RequestParam(name = "ownerUserId", required = false) UUID ownerUserId,
            @Valid @RequestBody AddBeneficiaryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        UUID targetUserId = ownerUserId;
        if (targetUserId == null && xUserId != null && !xUserId.isBlank()) {
            targetUserId = UUID.fromString(xUserId);
        }
        if (targetUserId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", "ownerUserId parameter or X-User-Id header is required", null, requestId));
        }

        BeneficiaryResponse response = accountService.addBeneficiary(targetUserId, request);
        return new ResponseEntity<>(ApiResponse.ok(response, requestId), HttpStatus.CREATED);
    }

    @GetMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(
            @RequestHeader(name = "X-User-Id", required = false) String xUserId,
            @RequestParam(name = "ownerUserId", required = false) UUID ownerUserId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        UUID targetUserId = ownerUserId;
        if (targetUserId == null && xUserId != null && !xUserId.isBlank()) {
            targetUserId = UUID.fromString(xUserId);
        }
        if (targetUserId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", "ownerUserId parameter or X-User-Id header is required", null, requestId));
        }

        List<BeneficiaryResponse> response = accountService.getBeneficiaries(targetUserId);
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
