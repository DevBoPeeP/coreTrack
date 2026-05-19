package com.notificationservice.dto.response;

import com.notificationservice.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private Notification.NotificationType type;
    private Notification.NotificationCategory category;
    private String title;
    private String message;
    private UUID referenceId;
    private Notification.ReferenceType referenceType;
    private boolean isRead;
    private Instant createdAt;
    private Instant readAt;
}
