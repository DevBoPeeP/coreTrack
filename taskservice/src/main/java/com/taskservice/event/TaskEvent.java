package com.taskservice.event;

import com.taskservice.model.Task;
import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private UUID taskId;
    private String type;
    private Instant timestamp;
}
