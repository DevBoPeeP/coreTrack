package com.taskservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectRequest {

    private UUID teamId;
    private List<ProjectRef> projects;  // Fixed: was List<Project> (entity) — now uses minimal DTO

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRef {
        private UUID id;
        private String name;
    }
}
