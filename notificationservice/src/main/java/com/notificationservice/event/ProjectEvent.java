package com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// PROJECT EVENT  (topic: project-events)
// types: CREATED, UPDATED, DELETED, TASKS_ADDED, TASKS_REMOVED
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
    private UUID projectId;
    private String type;
    private Instant timestamp;
}
