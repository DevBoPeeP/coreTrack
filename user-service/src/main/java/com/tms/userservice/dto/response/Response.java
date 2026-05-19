package com.tms.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
public class Response {
    private String responseCode;
    private String responseMessage;
}
