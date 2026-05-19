package com.taskservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private UUID id;
    private String content;
private UUID authorId;
    private UUID taskId;
    private UUID projectId;
    private Set<UUID> mentionedUserIds;
    private Instant createdAt;
    private Boolean deleted;
}
