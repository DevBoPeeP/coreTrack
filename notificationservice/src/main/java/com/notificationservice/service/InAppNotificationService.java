package com.notificationservice.service;

import com.notificationservice.dto.response.NotificationResponse;
import com.notificationservice.model.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.websocket.WebSocketNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationPublisher webSocketPublisher;

    @Value("${notification.in-app.enabled}")
    private boolean inAppEnabled;

    @Value("${notification.in-app.max-unread-per-user}")
    private int maxUnreadPerUser;

    /**
     * Saves an in-app notification to the DB and pushes it to the user's
     * WebSocket subscription in real time.
     */
    public void send(UUID userId, String title, String message,
                     Notification.NotificationCategory category,
                     UUID referenceId, Notification.ReferenceType referenceType) {

        if (!inAppEnabled) return;

        try {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(Notification.NotificationType.IN_APP)
                    .category(category)
                    .title(title)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);

            // Push to browser/app in real time via WebSocket
            webSocketPublisher.pushToUser(userId, mapToResponse(saved));

            log.info("In-app notification saved and pushed userId={} category={}",
                    userId, category);

        } catch (Exception e) {
            log.error("Failed to save in-app notification userId={} category={}",
                    userId, category, e);
        }
    }

    // ── Query methods called by the REST controller ───────────

    public Page<NotificationResponse> getNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public Page<NotificationResponse> getUnreadNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public boolean markAsRead(UUID notificationId, UUID userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        return updated > 0;
    }

    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    // ── Mapper ────────────────────────────────────────────────

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .category(n.getCategory())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
