package com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// TASK EVENT  (topic: task-events)
// types: CREATED, UPDATED, DELETED
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private UUID taskId;
    private String type;
    private Instant timestamp;
}
