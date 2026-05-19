package com.taskservice.dto.request;

import com.taskservice.model.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {
    private UUID taskId;
    private String title;
    private String description;
    private Task.TaskStatus taskStatus;
    private UUID assignedId;
    private LocalDateTime dueDate;


}
