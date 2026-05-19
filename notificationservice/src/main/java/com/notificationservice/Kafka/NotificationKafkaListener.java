package com.notificationservice.Kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.event.*;
import com.notificationservice.model.Notification;
import com.notificationservice.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaListener {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onTaskEvent(String payload, Acknowledgment ack) {
        try {
            TaskEvent event = objectMapper.readValue(payload, TaskEvent.class);
            log.info("action=consume_task_event status=processing type={} taskId={}", event.getType(), event.getTaskId());

            String title = "Task Update";
            String message = "There is an update on your task.";
            Notification.NotificationCategory category = Notification.NotificationCategory.TASK_UPDATED;

            switch (event.getType()) {
                case "CREATED" -> { title = "New Task Created"; message = "A new task has been assigned to you."; category = Notification.NotificationCategory.TASK_CREATED; }
                case "UPDATED" -> { title = "Task Updated"; message = "A task you are watching has been updated."; category = Notification.NotificationCategory.TASK_UPDATED; }
                case "DELETED" -> { title = "Task Deleted"; message = "A task has been removed."; category = Notification.NotificationCategory.TASK_DELETED; }
            }

            // NOTE: In a real flow, you should fetch the Assignee ID. Using event.getTaskId() as UserId is invalid!
            // Assuming the task service or event payload includes `assigneeId` moving forward:
            // dispatcher.dispatch(event.getAssigneeId(), title, message, category, event.getTaskId(), Notification.ReferenceType.TASK);

            ack.acknowledge();
            log.info("action=consume_task_event status=success taskId={}", event.getTaskId());
        } catch (Exception e) {
            log.error("action=consume_task_event status=failed payload={}", payload, e);
            ack.acknowledge(); // Acknowledge to prevent infinite loop on malformed data
        }
    }

    @KafkaListener(topics = "task.comment.created", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onCommentCreated(String payload, Acknowledgment ack) {
        try {
            CommentEvent event = objectMapper.readValue(payload, CommentEvent.class);
            log.info("action=consume_comment_created status=processing commentId={} taskId={}", event.getCommentId(), event.getTaskId());

            if (event.getMentionedUserIds() != null && !event.getMentionedUserIds().isEmpty()) {
                for (UUID mentionedUserId : event.getMentionedUserIds()) {
                    dispatcher.dispatch(
                            mentionedUserId,
                            "You were mentioned in a comment",
                            truncate(event.getContent(), 100),
                            Notification.NotificationCategory.COMMENT_MENTION,
                            event.getCommentId(),
                            Notification.ReferenceType.COMMENT
                    );
                }
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("action=consume_comment_created status=failed payload={}", payload, e);
            ack.acknowledge();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}