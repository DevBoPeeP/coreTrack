package com.tms.userservice.service;

import com.tms.userservice.dto.request.LoginRequest;
import com.tms.userservice.dto.response.AuthResponse;
import com.tms.userservice.exception.UserNotVerifiedException;
import com.tms.userservice.model.User;
import com.tms.userservice.repository.UserRepository;
import com.tms.userservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LogoutService tokenBlacklistService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, LogoutService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse login(LoginRequest request) {
        AuthResponse authResponse=new AuthResponse();
        try {
            String input = request.getUsernameOrEmail();

            Optional<User> optionalUser;

            if (input.contains("@")) {
                optionalUser = userRepository.findByEmail(input);
            } else {
                optionalUser = userRepository.findByUsername(input);
            }

            User user = optionalUser
                    .orElseThrow(() -> new AuthenticationException("Invalid login credentials"));


            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password");
            }


            if (!user.isVerified()) {
                throw new UserNotVerifiedException("User not verified. Please verify OTP before login.");
            }

            // Update last login timestamp
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Login successful for user: " + input);
            String token = jwtUtil.generateToken(user.getUsername());
            authResponse.setToken(token);
            authResponse.setResponseCode("00");
            authResponse.setResponseMessage("Login successful for user: " + user.getUsername());

            return authResponse;

        } catch (AuthenticationException | UserNotVerifiedException e) {
            log.info("Login failed for user: " + request.getUsernameOrEmail() + " - " + e);
            authResponse.setResponseCode("01");
            authResponse.setResponseMessage("Login failed: invalid username or password, or user not verified.");
            return authResponse;
        } catch (Exception e) {
            log.error("Unexpected error during login for user: " + request.getUsernameOrEmail(), e);
            authResponse.setResponseCode("99");
            authResponse.setResponseMessage("An unexpected error occurred. Please try again later.");
            return authResponse;
        }

    }

    public void logout(String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Logging out token: " + token);

            tokenBlacklistService.blacklistToken(token);
            log.info("Token blacklisted for logout");
        } else {
            throw new IllegalArgumentException("No token provided");
        }
    }
}

