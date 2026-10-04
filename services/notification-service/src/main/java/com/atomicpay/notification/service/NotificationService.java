package com.atomicpay.notification.service;

import com.atomicpay.notification.dto.NotificationResponse;
import com.atomicpay.notification.dto.TransactionDomainEvent;
import com.atomicpay.notification.entity.NotificationEntity;
import com.atomicpay.notification.entity.ProcessedEventEntity;
import com.atomicpay.notification.entity.ProcessedEventId;
import com.atomicpay.notification.repository.NotificationRepository;
import com.atomicpay.notification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String CONSUMER_NAME = "notification-service";

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationService(NotificationRepository notificationRepository, ProcessedEventRepository processedEventRepository) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void processTransactionEvent(TransactionDomainEvent event) {
        if (event == null || event.getEventId() == null) {
            log.warn("Received null or invalid event in notification service");
            return;
        }

        ProcessedEventId processedEventId = new ProcessedEventId(CONSUMER_NAME, event.getEventId());
        if (processedEventRepository.existsById(processedEventId)) {
            log.info("Event {} has already been processed by {}. Deduplicating and ignoring.", event.getEventId(), CONSUMER_NAME);
            return;
        }

        TransactionDomainEvent.EventPayload payload = event.getPayload();
        if (payload == null) {
            log.warn("Received event {} with null payload", event.getEventId());
            return;
        }

        String eventType = event.getEventType();
        String title;
        String message;

        switch (eventType != null ? eventType : "") {
            case "TRANSACTION_COMPLETED" -> {
                title = "Transfer Successful";
                message = String.format("Transfer of %s %s to account %s completed successfully.",
                        payload.getAmount(), payload.getCurrency(), payload.getReceiverAccountId());
            }
            case "TRANSACTION_COMPENSATED" -> {
                title = "Transfer Failed & Refunded";
                message = String.format("Transfer of %s %s failed. Funds have been compensated/refunded to your account.",
                        payload.getAmount(), payload.getCurrency());
            }
            case "TRANSACTION_FAILED" -> {
                title = "Transfer Failed";
                message = String.format("Transfer of %s %s failed: %s",
                        payload.getAmount(), payload.getCurrency(), payload.getFailureReason() != null ? payload.getFailureReason() : "Validation/Execution Error");
            }
            case "TRANSACTION_RECONCILIATION_REQUIRED" -> {
                title = "Transfer Pending Verification";
                message = String.format("Transfer of %s %s is undergoing status reconciliation.",
                        payload.getAmount(), payload.getCurrency());
            }
            default -> {
                title = "Account Activity Alert";
                message = String.format("Transaction update for %s %s.", payload.getAmount(), payload.getCurrency());
            }
        }

        // Create notification record for sender user/account
        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .userId(payload.getSenderAccountId()) // Using account ID as user context
                .transactionId(event.getTransactionId())
                .type(eventType != null ? eventType : "ALERT")
                .title(title)
                .message(message)
                .createdAt(OffsetDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Mark event as processed (Inbox Deduplication)
        processedEventRepository.save(new ProcessedEventEntity(CONSUMER_NAME, event.getEventId(), OffsetDateTime.now()));
        log.info("Successfully created notification {} for transaction event {}", notification.getId(), event.getEventId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        notification.setReadAt(OffsetDateTime.now());
        NotificationEntity updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }

    private NotificationResponse mapToResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .transactionId(entity.getTransactionId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
