package com.tms.userservice.service;

import com.tms.userservice.dto.response.OtpResponse;
import com.tms.userservice.dto.response.Response;
import com.tms.userservice.dto.request.VerifyUserRequest;
import com.tms.userservice.model.Otp;
import com.tms.userservice.model.User;
import com.tms.userservice.repository.OtpRepository;
import com.tms.userservice.repository.UserRepository;
import com.tms.userservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@Service
public class OtpService {
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Autowired
    private Environment environment;

    private static final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpRepository otpRepository,
                      PasswordEncoder passwordEncoder,
                      EmailService emailService,
                      UserRepository userRepository,
                      JwtUtil jwtUtil) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }


    public String generateOtp(String email) {
        try {
            int number = secureRandom.nextInt(1_000_000); // 0-999999
            String code = String.format("%06d", number);
            String hashed = passwordEncoder.encode(code);

            Otp otp = Otp.builder()
                    .email(email)
                    .codeHash(hashed)
                    .expiry(LocalDateTime.now().plusMinutes(5))
                    .createdAt(LocalDateTime.now())
                    .status(Otp.Status.ACTIVE)
                    .code(code)
                    .build();
            otpRepository.save(otp);

            log.info("Generated OTP for {}", email);
            return code;
        } catch (Exception e) {
            log.error("Failed to generate OTP for {}", email, e);
            throw new RuntimeException("Failed to generate OTP", e);
        }
    }


    public Response sendOtp(String email) {
        Response response = new Response();
        try {
            log.info("Sending OTP to {}", email);
            String otp = generateOtp(email);

            String otpText = environment.getProperty("otp.email.body");
            String subject = environment.getProperty("otp.email.subject");
            otpText = otpText.replace("{otp}", otp);

            boolean isSent = emailService.sendEmail(email, otpText, subject);

            if (isSent) {
                response.setResponseCode("00");
                response.setResponseMessage("OTP sent successfully to " + email);
            } else {
                response.setResponseCode("96");
                response.setResponseMessage("Failed to send OTP to " + email);
            }
        } catch (Exception e) {
            log.error("Failed to send OTP to {}", email, e);
            response.setResponseCode("99");
            response.setResponseMessage("Exception occurred while sending OTP");
        }
        return response;
    }


    public OtpResponse verifyOtp(VerifyUserRequest request) {
        OtpResponse response = new OtpResponse();
        try {
            log.info("Verifying OTP for {}", request.getEmail());
            Optional<Otp> otpOpt = otpRepository.findTopByEmailOrderByCreatedAtDesc(request.getEmail());

            if (otpOpt.isEmpty()) {
                response.setResponseCode("01");
                response.setResponseMessage("OTP not found");
                return response;
            }

            Otp otp = otpOpt.get();

            log.info("Found OTP with status {} for {} in {}", otp.getCodeHash(), request.getCode(), otp);

            if (otp.getStatus() == Otp.Status.EXPIRED) {
                response.setResponseCode("02");
                response.setResponseMessage("OTP expired");
                return response;
            } else if (otp.getStatus() == Otp.Status.USED) {
                response.setResponseCode("04");
                response.setResponseMessage("OTP already used");
                return response;
            } else if (otp.getStatus() != Otp.Status.ACTIVE) {
                response.setResponseCode("99");
                response.setResponseMessage("Invalid OTP status");
                return response;
            }

            if (otp.getExpiry().isBefore(LocalDateTime.now())) {
                otp.setStatus(Otp.Status.EXPIRED);
                otpRepository.save(otp);
                response.setResponseCode("02");
                response.setResponseMessage("OTP expired");
            } else if (otp.getCodeHash() == null || request.getCode() == null) {
                log.error("OTP or request code is null for {}", request.getEmail());
                response.setResponseCode("99");
                response.setResponseMessage("Invalid OTP state");
            } else if (!passwordEncoder.matches(request.getCode(), otp.getCodeHash())) {
                response.setResponseCode("01");
                response.setResponseMessage("Invalid OTP");
            } else {
                otp.setStatus(Otp.Status.USED);
                otpRepository.save(otp);

                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                user.setVerified(true);
                userRepository.save(user);


                String token = jwtUtil.generateToken(user.getUsername());

                response.setResponseCode("00");
                response.setResponseMessage("OTP verified successfully");
                response.setToken(token);
            }

        } catch (Exception e) {
            log.error("Error verifying OTP for {}", request.getEmail(), e);
            response.setResponseCode("99");
            response.setResponseMessage("Technical error occurred");
        }
        return response;
    }

}
