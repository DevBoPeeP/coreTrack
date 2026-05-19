package com.notificationservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationRequest {
    private Long id;
    private String userId;
    private String message;
    private String eventType;
    private Long referenceId; // taskId
    private Instant createdAt;
    private boolean read;
}
