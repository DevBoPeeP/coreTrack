package com.notificationservice.service;

import com.notificationservice.dto.request.RegisterDeviceTokenRequest;
import com.notificationservice.model.DeviceToken;
import com.notificationservice.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void registerToken(UUID userId, RegisterDeviceTokenRequest request) {
        // If token already exists, just make sure it's active and linked to this user
        deviceTokenRepository.findByToken(request.getToken()).ifPresentOrElse(
                existing -> {
                    existing.setUserId(userId);
                    existing.setActive(true);
                    existing.setLastUsedAt(Instant.now());
                    deviceTokenRepository.save(existing);
                    log.info("Device token updated for userId={}", userId);
                },
                () -> {
                    DeviceToken token = DeviceToken.builder()
                            .userId(userId)
                            .token(request.getToken())
                            .platform(request.getPlatform())
                            .active(true)
                            .lastUsedAt(Instant.now())
                            .build();
                    deviceTokenRepository.save(token);
                    log.info("Device token registered for userId={} platform={}",
                            userId, request.getPlatform());
                }
        );
    }

    public void deactivateToken(String token) {
        deviceTokenRepository.deactivateToken(token);
        log.info("Device token deactivated token={}", token);
    }

    public List<String> getActiveTokensForUser(UUID userId) {
        return deviceTokenRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
    }
}
