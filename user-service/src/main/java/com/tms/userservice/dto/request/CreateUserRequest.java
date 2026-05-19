package com.tms.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z-]+$", message = "First name must only contain letters or hyphen")
    private String firstName;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z-]+$", message = "Last name must only contain letters or hyphen")
    private String lastName;

    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}

