package com.taskservice.websocket;

import com.taskservice.event.CommentDeletedEvent;
import com.taskservice.event.CommentEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketCommentPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastComment(CommentEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/tasks/" + event.getTaskId() + "/comments",
                event
        );
    }

    public void broadcastDelete(CommentDeletedEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/tasks/" + event.getTaskId() + "/comments/delete",
                event
        );
    }
}