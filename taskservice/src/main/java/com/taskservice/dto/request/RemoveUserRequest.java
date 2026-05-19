package com.taskservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveUserRequest {
    private UUID teamId;
    private Set<UUID> memberIds;
    private Set<String> memberNames;
}
