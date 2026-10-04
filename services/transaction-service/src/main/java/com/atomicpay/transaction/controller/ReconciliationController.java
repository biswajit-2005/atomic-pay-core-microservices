package com.atomicpay.transaction.controller;

import com.atomicpay.transaction.dto.ApiResponse;
import com.atomicpay.transaction.dto.ReconciliationIssueResponse;
import com.atomicpay.transaction.dto.ReconciliationRunResponse;
import com.atomicpay.transaction.dto.ResolveIssueRequest;
import com.atomicpay.transaction.service.ReconciliationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ReconciliationRunResponse>> triggerReconciliation(HttpServletRequest request) {
        ReconciliationRunResponse response = reconciliationService.runReconciliation();
        return ResponseEntity.ok(ApiResponse.ok(response, getCorrelationId(request)));
    }

    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<List<ReconciliationIssueResponse>>> getIssues(
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest request
    ) {
        List<ReconciliationIssueResponse> issues = reconciliationService.getIssues(status);
        return ResponseEntity.ok(ApiResponse.ok(issues, getCorrelationId(request)));
    }

    @PatchMapping("/issues/{id}/resolve")
    public ResponseEntity<ApiResponse<ReconciliationIssueResponse>> resolveIssue(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ResolveIssueRequest resolveRequest,
            HttpServletRequest request
    ) {
        ReconciliationIssueResponse response = reconciliationService.resolveIssue(id, resolveRequest.getResolution());
        return ResponseEntity.ok(ApiResponse.ok(response, getCorrelationId(request)));
    }

    private String getCorrelationId(HttpServletRequest request) {
        String cid = request.getHeader("X-Correlation-ID");
        return cid != null && !cid.isBlank() ? cid : UUID.randomUUID().toString();
    }
}
