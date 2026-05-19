package com.taskservice.kafka;

import com.taskservice.event.CommentDeletedEvent;
import com.taskservice.event.CommentEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentKafkaListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "task.comment.created",
            groupId = "ws-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCommentCreated(CommentEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/tasks/" + event.getTaskId() + "/comments",
                    event
            );
            log.info("WebSocket broadcast COMMENT_CREATED commentId={} taskId={}",
                    event.getCommentId(), event.getTaskId());

        } catch (Exception e) {
            log.error("WebSocket broadcast failed for COMMENT_CREATED commentId={}",
                    event.getCommentId(), e);
        }
    }


    @KafkaListener(
            topics = "task.comment.deleted",
            groupId = "ws-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCommentDeleted(CommentDeletedEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/tasks/" + event.getTaskId() + "/comments/deleted",
                    event
            );
            log.info("WebSocket broadcast COMMENT_DELETED commentId={} taskId={}",
                    event.getCommentId(), event.getTaskId());

        } catch (Exception e) {
            log.error("WebSocket broadcast failed for COMMENT_DELETED commentId={}",
                    event.getCommentId(), e);
        }
    }
}