package com.taskservice.dto.request;

import com.taskservice.model.Project;
import com.taskservice.model.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String description;
    private UUID ownerId;
    private Team teamId;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private Project.ProjectStatus projectStatus;
    private Project.Priority priority;
}
