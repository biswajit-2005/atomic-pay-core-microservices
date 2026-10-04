package com.atomicpay.audit.controller;

import com.atomicpay.audit.dto.ApiResponse;
import com.atomicpay.audit.dto.AuditLogResponse;
import com.atomicpay.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(name = "transactionId", required = false) UUID transactionId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            HttpServletRequest request
    ) {
        List<AuditLogResponse> logs;
        if (transactionId != null) {
            logs = auditService.getAuditLogsForTransaction(transactionId);
        } else {
            logs = auditService.getAuditLogs(page, size);
        }
        return ResponseEntity.ok(ApiResponse.ok(logs, getCorrelationId(request)));
    }

    private String getCorrelationId(HttpServletRequest request) {
        String cid = request.getHeader("X-Correlation-ID");
        return cid != null && !cid.isBlank() ? cid : UUID.randomUUID().toString();
    }
}
