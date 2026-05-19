package com.taskservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    private String name;
    private String createdBy;
    private Instant createdAt;
    private List<UUID> memberIds;
    private List<String> memberNames;
    private List<ProjectRef> projects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRef {
        private UUID id;
        private String name;
    }
}
