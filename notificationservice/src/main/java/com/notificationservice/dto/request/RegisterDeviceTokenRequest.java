package com.notificationservice.dto.request;

import com.notificationservice.model.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String token;

    @NotNull(message = "Platform is required")
    private DeviceToken.DevicePlatform platform;
}
