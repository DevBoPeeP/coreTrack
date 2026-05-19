package com.notificationservice.dto.request;

import lombok.Data;

@Data
public class TaskEventPayload {
    private  String eventType;
    private Long taskId;
    private String taskTitle;
    private String message;
    private String assigneeId;
    private String assigneeEmail;


}
