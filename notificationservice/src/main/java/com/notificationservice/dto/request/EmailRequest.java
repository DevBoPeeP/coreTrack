package com.notificationservice.dto.request;


import lombok.Data;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String content;
}
