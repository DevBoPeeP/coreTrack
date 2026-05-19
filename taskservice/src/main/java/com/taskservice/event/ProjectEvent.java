package com.taskservice.event;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {

    private UUID projectId;

    // CREATED, UPDATED, DELETED, TASKS_ADDED, TASKS_REMOVED
    private String type;

    private Instant timestamp;
}