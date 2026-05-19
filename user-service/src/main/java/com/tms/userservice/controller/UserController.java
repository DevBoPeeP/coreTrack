package com.tms.userservice.controller;

import com.tms.userservice.dto.request.CreateUserRequest;
import com.tms.userservice.dto.request.UpdateProfileRequest;
import com.tms.userservice.dto.response.ApiResponse;
import com.tms.userservice.dto.response.Response;
import com.tms.userservice.dto.response.UpdateResponse;
import com.tms.userservice.dto.response.UserResponse;
import com.tms.userservice.dto.request.VerifyUserRequest;
import com.tms.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse user = userService.register(request);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Verification email sent")
                        .data(user)
                        .build()
        );
    }

    @PostMapping("/verifyUser")
    public ResponseEntity<Response> verifyUser(@RequestParam VerifyUserRequest request) {
        return ResponseEntity.ok(userService.verifyUser(request));
    }

    @GetMapping("/getCurrentUser")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        UserResponse userResponse =   userService.getCurrentUser(authHeader);
        return ResponseEntity.ok().body(userResponse);
    }

    @PostMapping("/updateProfile")
    public ResponseEntity<UpdateResponse> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        UpdateResponse updateResponse = userService.updateProfile(authHeader, request);
        return ResponseEntity.ok().body(updateResponse);
    }
}
