package com.taskservice.kafka;

import com.taskservice.event.CommentDeletedEvent;
import com.taskservice.event.CommentEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentKafkaProducer {


    private final KafkaTemplate<String, CommentEvent> commentKafkaTemplate;
    private final KafkaTemplate<String, CommentDeletedEvent> commentDeletedKafkaTemplate;

    private static final String COMMENT_CREATED_TOPIC = "task.comment.created";
    private static final String COMMENT_DELETED_TOPIC = "task.comment.deleted";

    public void publishCreated(CommentEvent event) {
        try {
            commentKafkaTemplate.send(
                    COMMENT_CREATED_TOPIC,
                    event.getTaskId().toString(),
                    event
            );
            log.info("Published COMMENT_CREATED commentId={} taskId={}",
                    event.getCommentId(), event.getTaskId());

        } catch (KafkaException e) {
            log.error("Failed to publish COMMENT_CREATED commentId={}", event.getCommentId(), e);
            throw e;
        }
    }

    public void publishDeleted(CommentDeletedEvent event) {
        try {
            commentDeletedKafkaTemplate.send(
                    COMMENT_DELETED_TOPIC,
                    event.getTaskId().toString(),  // partition key
                    event
            );
            log.info("Published COMMENT_DELETED commentId={} taskId={}",
                    event.getCommentId(), event.getTaskId());

        } catch (KafkaException e) {
            log.error("Failed to publish COMMENT_DELETED commentId={}", event.getCommentId(), e);
            throw e;
        }
    }
}