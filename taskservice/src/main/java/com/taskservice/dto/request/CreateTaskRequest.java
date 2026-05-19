package com.taskservice.dto.request;

import com.taskservice.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private LocalDateTime dueDate;

    private UUID assigneeId;

    private String createdBy;

    private UUID projectId;

    private UUID id;

    private Boolean personal;

    private Instant createdAt;

    private Instant updatedAt;

    private Task.TaskStatus taskStatus;
}
