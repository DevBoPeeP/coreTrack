package com.taskservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {

        log.info("WebSocket client connected");
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {

        log.info("WebSocket client disconnected");
    }
}