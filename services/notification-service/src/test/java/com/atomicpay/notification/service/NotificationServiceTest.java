package com.atomicpay.notification.service;

import com.atomicpay.notification.dto.NotificationResponse;
import com.atomicpay.notification.dto.TransactionDomainEvent;
import com.atomicpay.notification.entity.NotificationEntity;
import com.atomicpay.notification.entity.ProcessedEventEntity;
import com.atomicpay.notification.entity.ProcessedEventId;
import com.atomicpay.notification.repository.NotificationRepository;
import com.atomicpay.notification.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID eventId;
    private UUID transactionId;
    private TransactionDomainEvent event;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        TransactionDomainEvent.EventPayload payload = new TransactionDomainEvent.EventPayload(
                userId, UUID.randomUUID(), new BigDecimal("1000.0000"), "INR", "COMPLETED", null
        );

        event = new TransactionDomainEvent(
                eventId, "TRANSACTION_COMPLETED", 1, transactionId, "cid-123", OffsetDateTime.now(), payload
        );
    }

    @Test
    @DisplayName("Should create notification and save processed event for new transaction event")
    void processTransactionEvent_NewEvent_Success() {
        when(processedEventRepository.existsById(any(ProcessedEventId.class))).thenReturn(false);

        notificationService.processTransactionEvent(event);

        verify(notificationRepository, times(1)).save(argThat(n -> {
            assertEquals(userId, n.getUserId());
            assertEquals(transactionId, n.getTransactionId());
            assertEquals("TRANSACTION_COMPLETED", n.getType());
            assertTrue(n.getTitle().contains("Transfer Successful"));
            return true;
        }));

        verify(processedEventRepository, times(1)).save(any(ProcessedEventEntity.class));
    }

    @Test
    @DisplayName("Should deduplicate and ignore already processed event")
    void processTransactionEvent_DuplicateEvent_Ignored() {
        when(processedEventRepository.existsById(any(ProcessedEventId.class))).thenReturn(true);

        notificationService.processTransactionEvent(event);

        verify(notificationRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch notifications for user")
    void getNotificationsForUser_Success() {
        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .transactionId(transactionId)
                .type("TRANSACTION_COMPLETED")
                .title("Transfer Successful")
                .message("Transfer completed")
                .createdAt(OffsetDateTime.now())
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getNotificationsForUser(userId);

        assertEquals(1, result.size());
        assertEquals("Transfer Successful", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should mark notification as read")
    void markAsRead_Success() {
        UUID notifId = UUID.randomUUID();
        NotificationEntity notification = NotificationEntity.builder()
                .id(notifId)
                .userId(userId)
                .transactionId(transactionId)
                .type("ALERT")
                .title("Alert")
                .message("Msg")
                .build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(notifId);

        assertNotNull(response.getReadAt());
        verify(notificationRepository, times(1)).save(notification);
    }
}
