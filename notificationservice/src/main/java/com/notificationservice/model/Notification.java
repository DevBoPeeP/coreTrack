package com.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "userId"),
        @Index(name = "idx_notifications_read", columnList = "userId, isRead")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Who receives this notification
    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    // Reference to the entity that triggered this notification
    private UUID referenceId;      // taskId / projectId / teamId / commentId

    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;   // TASK, PROJECT, TEAM, COMMENT

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    private Instant readAt;

    // ── Enums ────────────────────────────────────────────────

    public enum NotificationType {
        IN_APP,
        EMAIL,
        PUSH
    }

    public enum NotificationCategory {
        // Task events
        TASK_CREATED,
        TASK_UPDATED,
        TASK_DELETED,
        TASK_ASSIGNED,
        // Project events
        PROJECT_CREATED,
        PROJECT_UPDATED,
        PROJECT_DELETED,
        PROJECT_TASKS_ADDED,
        PROJECT_TASKS_REMOVED,
        // Team events
        TEAM_CREATED,
        TEAM_DELETED,
        TEAM_USER_ADDED,
        TEAM_USER_REMOVED,
        TEAM_PROJECT_ADDED,
        TEAM_PROJECT_REMOVED,
        // Comment events
        COMMENT_CREATED,
        COMMENT_DELETED,
        COMMENT_MENTION
    }

    public enum ReferenceType {
        TASK,
        PROJECT,
        TEAM,
        COMMENT
    }
}
