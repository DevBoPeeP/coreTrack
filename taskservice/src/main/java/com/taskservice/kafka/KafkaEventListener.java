package com.taskservice.kafka;

import com.taskservice.event.ProjectEvent;
import com.taskservice.event.TaskEvent;
import com.taskservice.event.TeamEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaEventListener {


    @KafkaListener(
            topics = "task-events",
            groupId = "taskservice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTaskEvent(TaskEvent event) {
        log.info("TASK EVENT RECEIVED → id={} type={} time={}",
                event.getTaskId(), event.getType(), event.getTimestamp());
    }

    @KafkaListener(
            topics = "project-events",
            groupId = "taskservice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onProjectEvent(ProjectEvent event) {
        log.info("PROJECT EVENT RECEIVED → id={} type={} time={}",
                event.getProjectId(), event.getType(), event.getTimestamp());
    }


    @KafkaListener(
            topics = "team-events",
            groupId = "taskservice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTeamEvent(TeamEvent event) {
        log.info("TEAM EVENT RECEIVED → id={} type={} time={}",
                event.getTeamId(), event.getType(), event.getTimestamp());
    }
}