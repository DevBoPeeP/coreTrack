package com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// COMMENT EVENT  (topic: task.comment.created)
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    private UUID commentId;
    private UUID taskId;
    private UUID projectId;
    private UUID authorId;
    private Set<UUID> mentionedUserIds;
    private String content;
    private Instant createdAt;
}
