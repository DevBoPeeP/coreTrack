package com.tms.userservice.dto.response;

import lombok.Data;

@Data
public class AuthResponse extends Response{
    private String token;

}
