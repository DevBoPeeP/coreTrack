package com.taskservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEvent implements Serializable {
    private UUID commentId;
    private UUID taskId;
    private UUID projectId;
    private UUID authorId;
    private Set<UUID> mentionedUserIds;
    private String content;
    private Instant createdAt;
}
