package com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// COMMENT DELETED EVENT  (topic: task.comment.deleted)
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDeletedEvent {
    private UUID commentId;
    private UUID taskId;
}
