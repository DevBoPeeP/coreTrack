package com.tms.userservice.dto.response;

import lombok.Data;

@Data
public class OtpResponse extends Response{
    private String token;
}
