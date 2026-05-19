package com.taskservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private UUID taskId;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID authorId;

    @ElementCollection
    @CollectionTable(
            name = "comment_mentions",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Column(name = "mentioned_user_id")
    private Set<UUID> mentionedUserIds;

    @Column(nullable = false)
    private Instant createdAt;

    // ✅ Soft delete fields
    @Column(nullable = false)
    private boolean deleted = false;

    private Instant deletedAt;
}