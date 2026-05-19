package com.taskservice.dto.response;

import lombok.Data;

@Data
public class TaskResponse {
    public enum status{TODO, IN_PROGRESS, DONE };
    public Long id;
    public String name;
    public String description;
    public Long projectId;
    public Long assignedTo;
    public status taskStatus = status.TODO;
}
