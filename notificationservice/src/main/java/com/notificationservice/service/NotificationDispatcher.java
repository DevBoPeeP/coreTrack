package com.notificationservice.service;

import com.notificationservice.client.UserServiceClient;
import com.notificationservice.client.UserServiceClient.UserDto;
import com.notificationservice.email.EmailService;
import com.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final InAppNotificationService inAppService;
    private final EmailService emailService;
    private final PushNotificationService pushService;
    private final DeviceTokenService deviceTokenService;
    private final UserServiceClient userServiceClient;

    public void dispatch(UUID userId,
                         String title,
                         String message,
                         Notification.NotificationCategory category,
                         UUID referenceId,
                         Notification.ReferenceType referenceType) {

        if(userId == null) {
            log.warn("action=dispatch_notification status=skipped reason=null_userId category={}", category);
            return;
        }

        try {
            // 1. In-app (Always sent)
            inAppService.send(userId, title, message, category, referenceId, referenceType);

            // 2. Fetch User Preferences safely
            UserDto user = null;
            try {
                user = userServiceClient.getUserById(userId);
            } catch (Exception ex) {
                log.error("action=fetch_user_preferences status=failed userId={} error={}", userId, ex.getMessage());
                // Proceed with default fallback (Push/Email might fail without valid user object)
            }

            if (user == null) {
                log.warn("action=dispatch_notification status=partial_delivery reason=user_not_found userId={}", userId);
                return;
            }

            // 3. Email
            if (user.isEmailNotificationsEnabled()) {
                dispatchEmail(user, title, message, category, referenceId);
            } else {
                log.debug("action=dispatch_email status=skipped reason=preferences_disabled userId={}", userId);
            }

            // 4. Push
            if (user.isPushNotificationsEnabled()) {
                dispatchPush(userId, title, message, category);
            } else {
                log.debug("action=dispatch_push status=skipped reason=preferences_disabled userId={}", userId);
            }

        } catch (Exception e) {
            log.error("action=dispatch_notification status=failed userId={} category={}", userId, category, e);
        }
    }

    private void dispatchEmail(UserDto user, String title, String message, Notification.NotificationCategory category, UUID referenceId) {
        try {
            switch (category) {
                case TASK_CREATED -> emailService.sendTaskCreatedEmail(user.getEmail(), user.getFullName(), title, "Your Project");
                case TASK_ASSIGNED -> emailService.sendTaskAssignedEmail(user.getEmail(), user.getFullName(), title, "A team member");
                case TASK_UPDATED -> emailService.sendTaskUpdatedEmail(user.getEmail(), user.getFullName(), title);
                case COMMENT_MENTION -> emailService.sendCommentMentionEmail(user.getEmail(), user.getFullName(), "A team member", title, message);
                case TEAM_USER_ADDED -> emailService.sendTeamAddedEmail(user.getEmail(), user.getFullName(), title);
                case PROJECT_CREATED -> emailService.sendProjectCreatedEmail(user.getEmail(), user.getFullName(), title);
                default -> emailService.sendTemplatedEmail(user.getEmail(), title, "generic-notification", buildGenericContext(user.getFullName(), title, message));
            }
            log.info("action=dispatch_email status=success userId={} category={}", user.getId(), category);
        } catch (Exception e) {
            log.error("action=dispatch_email status=failed userId={}", user.getId(), e);
        }
    }

    private void dispatchPush(UUID userId, String title, String message, Notification.NotificationCategory category) {
        try {
            List<String> tokens = deviceTokenService.getActiveTokensForUser(userId);
            if (tokens.isEmpty()) {
                log.debug("action=dispatch_push status=skipped reason=no_active_tokens userId={}", userId);
                return;
            }
            pushService.sendToMultipleDevices(tokens, title, message, category.name());
            log.info("action=dispatch_push status=success userId={} tokensCount={}", userId, tokens.size());
        } catch (Exception e) {
            log.error("action=dispatch_push status=failed userId={}", userId, e);
        }
    }

    private org.thymeleaf.context.Context buildGenericContext(String name, String title, String message) {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("recipientName", name);
        ctx.setVariable("title", title);
        ctx.setVariable("message", message);
        return ctx;
    }
}