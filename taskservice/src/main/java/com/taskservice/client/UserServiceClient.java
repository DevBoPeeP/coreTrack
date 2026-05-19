package com.taskservice.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/internal/users/username/{username}")
    UUID getUserIdByUsername(@PathVariable String username);

}


@Data
class UserDTO {
    private UUID id;
    private String username;
    private String name;
    private String email;


}


