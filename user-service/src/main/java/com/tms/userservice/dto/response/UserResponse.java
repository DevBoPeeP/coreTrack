package com.tms.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean verified;
    private boolean emailSent;
    private LocalDateTime createdAt;
}