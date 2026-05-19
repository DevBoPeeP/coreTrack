package com.notificationservice.websocket;

import com.notificationservice.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pushes a notification to a specific user's WebSocket subscription.
     * Client subscribes to: /topic/notifications/{userId}
     */
    public void pushToUser(UUID userId, NotificationResponse notification) {
        try {
            String destination = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            log.debug("WebSocket push → userId={} notificationId={}",
                    userId, notification.getId());
        } catch (Exception e) {
            log.error("WebSocket push failed for userId={}", userId, e);
        }
    }

    /**
     * Pushes an unread count update to a user.
     * Client subscribes to: /topic/notifications/{userId}/count
     */
    public void pushUnreadCount(UUID userId, long count) {
        try {
            String destination = "/topic/notifications/" + userId + "/count";
            messagingTemplate.convertAndSend(destination, count);
        } catch (Exception e) {
            log.error("WebSocket unread count push failed for userId={}", userId, e);
        }
    }
}
