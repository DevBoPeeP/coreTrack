package com.taskservice.controller;

import com.taskservice.dto.request.CreateCommentRequest;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.dto.response.CommentResponse;
import com.taskservice.service.CommentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentService commentService;

    /**
     * Add Comment
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {

        ApiResponse<CommentResponse> response = commentService.addComment(taskId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Comment (Soft Delete)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable UUID commentId) {

        ApiResponse<String> response = commentService.deleteComment(commentId);

        return ResponseEntity.ok(response);
    }
}