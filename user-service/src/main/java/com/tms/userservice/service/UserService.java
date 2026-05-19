package com.tms.userservice.service;

import com.tms.userservice.dto.request.CreateUserRequest;
import com.tms.userservice.dto.request.UpdateProfileRequest;
import com.tms.userservice.dto.response.Response;
import com.tms.userservice.dto.response.UpdateResponse;
import com.tms.userservice.dto.response.UserResponse;
import com.tms.userservice.dto.request.VerifyUserRequest;
import com.tms.userservice.exception.InvalidPhoneNumberException;
import com.tms.userservice.exception.UserNotFoundException;
import com.tms.userservice.exception.DuplicateUsernameException;
import com.tms.userservice.model.User;
import com.tms.userservice.repository.UserRepository;
import com.tms.userservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.NumberParseException;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    private boolean isValidPhoneNumber(String phoneNumber){
        try{
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, "NG");
            return phoneUtil.isValidNumber(number);
        }catch (NumberParseException e){
            return false;
        }
    }

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }


    public UserResponse register(CreateUserRequest request) {
        UserResponse userResponse = new UserResponse();
        try {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .verified(false)
                    .build();

            User saved = userRepository.save(user);

            try {
                otpService.sendOtp(saved.getEmail());
            } catch (Exception e) {
                log.error("OTP sending failed for {}", saved.getEmail(), e);
                throw new RuntimeException("Failed to send verification email");
            }

            return new UserResponse(
                    saved.getId(),
                    saved.getFirstName(),
                    saved.getLastName(),
                    saved.isActive(),
                    saved.isVerified(),
                    true,
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            log.error("User registration failed: {}", request.getEmail(), e);
            throw new RuntimeException("User registration failed", e);
        }
    }

    public Response verifyUser(VerifyUserRequest request) {
        Response otpResponse = otpService.verifyOtp(request);
       try {
           if ("00".equals(otpResponse.getResponseCode())) {
               userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                   user.setVerified(true);
                   userRepository.save(user);
                   log.info("User {} verified successfully", request.getEmail());
               });
           }
       }catch (Exception e) {
           log.error("Failed to update user verification status for {}", request.getEmail(), e);
           otpResponse.setResponseCode("99");
           otpResponse.setResponseMessage("Exception occurred while updating user status");
       }
        return otpResponse;
    }


    public UserResponse getCurrentUser(String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setActive(user.isActive());
            userResponse.setVerified(user.isVerified());

            return userResponse;
        } catch (Exception e) {
            log.error("Failed to retrieve current user: {}", username, e);
            throw new RuntimeException("Failed to retrieve current user", e);
        }
    }


    public UpdateResponse updateProfile(String authHeader, UpdateProfileRequest request) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        try{
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));


        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateUsernameException("Username already taken");
            }
        }


        if (!isValidPhoneNumber(request.getPhoneNumber())) {
            throw new InvalidPhoneNumberException("Invalid phone number format");
        }

        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setJobTitle(request.getJobTitle());

        User updateProfile = userRepository.save(user);
        log.info("User profile updated successfully: {}", username);

        return new UpdateResponse(
                updateProfile.getUsername(),
                updateProfile.getPhoneNumber(),
                updateProfile.getJobTitle()
        );
    }catch(Exception e){
        log.error("Failed to update profile for user: {}", username, e);
        throw new RuntimeException("Profile update failed", e);
    }

    }
}

