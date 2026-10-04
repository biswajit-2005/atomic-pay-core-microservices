package com.atomicpay.notification.controller;

import com.atomicpay.notification.dto.ApiResponse;
import com.atomicpay.notification.dto.NotificationResponse;
import com.atomicpay.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(name = "userId", required = false) UUID paramUserId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId,
            HttpServletRequest request
    ) {
        UUID targetUserId = paramUserId;
        if (targetUserId == null && xUserId != null && !xUserId.isBlank()) {
            targetUserId = UUID.fromString(xUserId);
        }

        if (targetUserId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "userId parameter or X-User-Id header is required", java.time.OffsetDateTime.now(), getCorrelationId(request)));
        }

        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(notifications, getCorrelationId(request)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable("id") UUID id,
            HttpServletRequest request
    ) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok(response, getCorrelationId(request)));
    }

    private String getCorrelationId(HttpServletRequest request) {
        String cid = request.getHeader("X-Correlation-ID");
        return cid != null && !cid.isBlank() ? cid : UUID.randomUUID().toString();
    }
}
