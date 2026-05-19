package com.taskservice.service;

import com.taskservice.client.UserServiceClient;
import com.taskservice.dto.request.CreateCommentRequest;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.dto.response.CommentResponse;
import com.taskservice.event.CommentDeletedEvent;
import com.taskservice.event.CommentEvent;
import com.taskservice.kafka.CommentKafkaProducer;
import com.taskservice.model.Comment;
import com.taskservice.model.Task;
import com.taskservice.repository.CommentRepository;
import com.taskservice.repository.TaskRepository;
import com.taskservice.security.CustomUserPrincipal;
import com.taskservice.websocket.WebSocketCommentPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.kafka.KafkaException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentKafkaProducer kafkaProducer;
    private final UserServiceClient userServiceClient;
    private final WebSocketCommentPublisher webSocketPublisher;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");


    public ApiResponse<CommentResponse> addComment(UUID taskId, CreateCommentRequest request) {
        try {
            if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ApiResponse.<CommentResponse>builder()
                        .responseCode("01")
                        .responseMessage("Comment content is required")
                        .data(null)
                        .build();
            }

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

            UUID authorId = getCurrentUserId();  // Fixed: now handles String principal safely

            Set<UUID> mentionedUsers = extractMentions(request.getContent());

            Comment comment = Comment.builder()
                    .id(UUID.randomUUID())
                    .content(request.getContent())
                    .taskId(task.getId())
                    .projectId(task.getProject().getId())
                    .authorId(authorId)
                    .mentionedUserIds(mentionedUsers)
                    .createdAt(Instant.now())
                    .deleted(false)
                    .build();

            Comment savedComment = commentRepository.save(comment);

            CommentEvent event = CommentEvent.builder()
                    .commentId(savedComment.getId())
                    .taskId(task.getId())
                    .projectId(task.getProject().getId())
                    .authorId(authorId)
                    .mentionedUserIds(mentionedUsers)
                    .content(savedComment.getContent())
                    .createdAt(savedComment.getCreatedAt())
                    .build();

            publishKafkaEvent(event);

            webSocketPublisher.broadcastComment(event);

            CommentResponse response = mapToResponse(savedComment);

            log.info("Comment created successfully commentId={} taskId={}", savedComment.getId(), taskId);

            return ApiResponse.<CommentResponse>builder()
                    .responseCode("00")
                    .responseMessage("Comment created successfully")
                    .data(response)
                    .build();

        } catch (DataAccessException e) {
            log.error("ADD COMMENT DB ERROR taskId={}", taskId, e);
            return ApiResponse.<CommentResponse>builder()
                    .responseCode("98")
                    .responseMessage("Database error: " + e.getMessage())
                    .data(null)
                    .build();

        } catch (KafkaException e) {
            log.error("ADD COMMENT KAFKA ERROR taskId={}", taskId, e);
            return ApiResponse.<CommentResponse>builder()
                    .responseCode("97")
                    .responseMessage("Comment saved but event publish failed: " + e.getMessage())
                    .data(null)
                    .build();

        } catch (Exception e) {
            log.error("ADD COMMENT ERROR taskId={}", taskId, e);
            return ApiResponse.<CommentResponse>builder()
                    .responseCode("99")
                    .responseMessage("Error creating comment: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }


    public ApiResponse<String> deleteComment(UUID commentId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

            UUID currentUser = getCurrentUserId();  // Fixed: now handles String principal safely

            if (!comment.getAuthorId().equals(currentUser)) {
                return ApiResponse.<String>builder()
                        .responseCode("02")
                        .responseMessage("You are not authorized to delete this comment")
                        .data(null)
                        .build();
            }

            comment.setDeleted(true);
            comment.setDeletedAt(Instant.now());

            commentRepository.save(comment);

            CommentDeletedEvent event = CommentDeletedEvent.builder()
                    .commentId(commentId)
                    .taskId(comment.getTaskId())
                    .build();

            kafkaProducer.publishDeleted(event);

            webSocketPublisher.broadcastDelete(event);

            log.info("Comment deleted commentId={}", commentId);

            return ApiResponse.<String>builder()
                    .responseCode("00")
                    .responseMessage("Comment deleted successfully")
                    .data(commentId.toString())
                    .build();

        } catch (DataAccessException e) {
            log.error("DELETE COMMENT DB ERROR commentId={}", commentId, e);
            return ApiResponse.<String>builder()
                    .responseCode("98")
                    .responseMessage("Database error: " + e.getMessage())
                    .data(null)
                    .build();

        } catch (KafkaException e) {
            log.error("DELETE COMMENT KAFKA ERROR commentId={}", commentId, e);
            return ApiResponse.<String>builder()
                    .responseCode("97")
                    .responseMessage("Comment deleted but event publish failed: " + e.getMessage())
                    .data(null)
                    .build();

        } catch (Exception e) {
            log.error("DELETE COMMENT ERROR commentId={}", commentId, e);
            return ApiResponse.<String>builder()
                    .responseCode("99")
                    .responseMessage("Error deleting comment: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }


    private void publishKafkaEvent(CommentEvent event) {
        try {
            kafkaProducer.publishCreated(event);
        } catch (KafkaException ex) {
            log.error("Kafka publish failed for commentId={}", event.getCommentId(), ex);
            throw ex;
        }
    }


    private Set<UUID> extractMentions(String content) {
        Set<UUID> mentionedUsers = new HashSet<>();
        try {
            Matcher matcher = MENTION_PATTERN.matcher(content);
            while (matcher.find()) {
                String username = matcher.group(1);
                UUID userId = userServiceClient.getUserIdByUsername(username);
                if (userId != null) {
                    mentionedUsers.add(userId);
                }
            }
        } catch (Exception e) {
            log.warn("Mention parsing failed, continuing without mentions", e);
        }
        return mentionedUsers;
    }


    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserPrincipal customPrincipal) {
            // Correct path — JWT filter is building CustomUserPrincipal properly
            return customPrincipal.getUserId();
        }

        if (principal instanceof String principalStr) {
            // JWT filter is setting principal as a plain String (username or "anonymousUser")
            // This means the JWT filter needs to be updated to build CustomUserPrincipal.
            // See JwtAuthFilter — extract userId claim and wrap in CustomUserPrincipal.
            if ("anonymousUser".equals(principalStr)) {
                throw new RuntimeException("User not authenticated — anonymous request");
            }
            log.error("Principal is a String '{}' — JWT filter must build CustomUserPrincipal instead. " +
                    "Ensure userId claim is present in token and filter wraps it in CustomUserPrincipal.", principalStr);
            throw new RuntimeException(
                    "Security configuration error: principal is a String, not CustomUserPrincipal. " +
                            "Check your JWT filter configuration."
            );
        }

        // Fallback for any other unexpected principal type
        throw new RuntimeException(
                "Unknown principal type: " + principal.getClass().getName()
        );
    }


    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .taskId(comment.getTaskId())
                .projectId(comment.getProjectId())
                .mentionedUserIds(comment.getMentionedUserIds())
                .createdAt(comment.getCreatedAt())
                .deleted(comment.isDeleted())
                .build();
    }
}