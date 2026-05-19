package com.notificationservice.controller;

import com.notificationservice.dto.request.RegisterDeviceTokenRequest;
import com.notificationservice.dto.response.ApiResponse;
import com.notificationservice.dto.response.NotificationResponse;
import com.notificationservice.security.CustomUserPrincipal;
import com.notificationservice.service.DeviceTokenService;
import com.notificationservice.service.InAppNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final InAppNotificationService inAppService;
    private final DeviceTokenService deviceTokenService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("action=fetch_notifications status=initiated userId={}", principal.getUserId());
        Page<NotificationResponse> notifications = inAppService.getNotifications(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("action=fetch_unread_notifications status=initiated userId={}", principal.getUserId());
        Page<NotificationResponse> notifications = inAppService.getUnreadNotifications(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        long count = inAppService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id) {

        log.info("action=mark_read status=initiated notificationId={} userId={}", id, principal.getUserId());
        boolean updated = inAppService.markAsRead(id, principal.getUserId());

        if (!updated) {
            log.warn("action=mark_read status=failed reason=not_found_or_unauthorized notificationId={} userId={}", id, principal.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("04", "Notification not found or access denied", HttpStatus.NOT_FOUND));
        }

        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.info("action=mark_all_read status=initiated userId={}", principal.getUserId());
        int count = inAppService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(count + " notifications marked as read", null));
    }

    @PostMapping("/device-token")
    public ResponseEntity<ApiResponse<String>> registerDeviceToken(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RegisterDeviceTokenRequest request) {

        log.info("action=register_device_token status=initiated platform={} userId={}", request.getPlatform(), principal.getUserId());
        deviceTokenService.registerToken(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Device token registered successfully", null));
    }

    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<ApiResponse<String>> deregisterDeviceToken(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String token) {

        log.info("action=deregister_device_token status=initiated userId={}", principal.getUserId());
        deviceTokenService.deactivateToken(token);
        return ResponseEntity.ok(ApiResponse.success("Device token deregistered successfully", null));
    }
}