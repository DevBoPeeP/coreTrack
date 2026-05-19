package com.taskservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name("task-events")
                .partitions(1)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic projectEventsTopic() {
        return TopicBuilder.name("project-events")
                .partitions(1)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic teamEventsTopic() {
        return TopicBuilder.name("team-events")
                .partitions(1)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic commentCreatedTopic() {
        return TopicBuilder.name("task.comment.created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentDeletedTopic() {
        return TopicBuilder.name("task.comment.deleted")
                .partitions(1)
                .replicas(1)
                .build();
    }
}