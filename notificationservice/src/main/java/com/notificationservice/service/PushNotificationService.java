package com.notificationservice.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    @Value("${notification.push.enabled}")
    private boolean pushEnabled;


    /**
     * Sends a push notification to a single device token.
     */
    @Async("notificationExecutor")
    public void sendToDevice(String deviceToken, String title,
                             String body, String category) {
        if (!pushEnabled || firebaseMessaging == null) {
            log.debug("Push notifications disabled — skipping");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("category", category)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Push sent to token={} response={}", deviceToken, response);

        } catch (FirebaseMessagingException e) {
            handleFirebaseError(deviceToken, e);
        } catch (Exception e) {
            log.error("Unexpected error sending push to token={}", deviceToken, e);
        }
    }

    /**
     * Sends a push notification to multiple device tokens at once (multicast).
     */
    @Async("notificationExecutor")
    public void sendToMultipleDevices(List<String> deviceTokens, String title,
                                      String body, String category) {
        if (!pushEnabled || firebaseMessaging == null || deviceTokens.isEmpty()) {
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("category", category)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Multicast push sent to {} devices. Success={} Failure={}",
                    deviceTokens.size(),
                    response.getSuccessCount(),
                    response.getFailureCount());

            // Log individual failures
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    log.warn("Push failed for token={} error={}",
                            deviceTokens.get(i),
                            responses.get(i).getException().getMessage());
                }
            }

        } catch (FirebaseMessagingException e) {
            log.error("Multicast push failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles Firebase-specific errors, e.g. expired/invalid tokens.
     */
    private void handleFirebaseError(String token, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            // Token is no longer valid — caller should deactivate it
            log.warn("Invalid/expired FCM token={} — should be deactivated", token);
        } else {
            log.error("Firebase error sending push to token={} code={} msg={}",
                    token, errorCode, e.getMessage());
        }
    }
}
