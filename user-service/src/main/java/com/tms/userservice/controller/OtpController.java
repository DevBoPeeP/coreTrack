package com.tms.userservice.controller;

import com.tms.userservice.dto.response.Response;
import com.tms.userservice.dto.request.VerifyUserRequest;
import com.tms.userservice.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@Slf4j
public class OtpController {
    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }


    @PostMapping("/send")
    public ResponseEntity<Response> sendOtp(@RequestParam String email) {
        log.info("Received request to send OTP to email: {}", email);
        Response response=otpService.sendOtp(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Response> verifyOtp(@RequestBody VerifyUserRequest request) {
        log.info("Received request to verify OTP for email: {}", request.getEmail());
            return ResponseEntity.ok(otpService.verifyOtp(request));

    }
}
