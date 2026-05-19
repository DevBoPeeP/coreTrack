package com.notificationservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserDto getUserById(@PathVariable UUID userId);

    @GetMapping("/api/users/username/{username}")
    UserDto getUserByUsername(@PathVariable String username);


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserDto {
        private UUID id;
        private String username;
        private String email;
        private String fullName;
        private boolean emailNotificationsEnabled;
        private boolean pushNotificationsEnabled;
    }
}
