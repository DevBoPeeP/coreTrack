package com.tms.userservice.controller;

import com.tms.userservice.dto.request.LoginRequest;
import com.tms.userservice.dto.response.AuthResponse;
import com.tms.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok().body(authResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Received logout request with Authorization header: {}", authHeader);
            authService.logout(authHeader);
            return ResponseEntity.ok("Logout successful");

    }


}
