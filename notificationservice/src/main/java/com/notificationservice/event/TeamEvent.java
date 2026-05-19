package com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// TEAM EVENT  (topic: team-events)
// types: CREATED, DELETED, USER_ADDED, USER_REMOVED,
//        PROJECT_ADDED, PROJECT_REMOVED
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamEvent {
    private UUID teamId;
    private String type;
    private Instant timestamp;
}
