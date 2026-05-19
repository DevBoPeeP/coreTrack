package com.taskservice.dto.request;

import com.taskservice.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UpdateProjectRequest {
    private UUID projectId;
    private String name;
    private String description;
    private Project.ProjectStatus status;
    private Project.Priority priority;


}
