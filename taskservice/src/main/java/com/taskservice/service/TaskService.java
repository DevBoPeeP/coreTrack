package com.taskservice.service;

import com.taskservice.client.UserServiceClient;
import com.taskservice.dto.request.CreateTaskRequest;
import com.taskservice.dto.request.UpdateTaskRequest;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.event.TaskEvent;
import com.taskservice.model.Project;
import com.taskservice.model.Task;
import com.taskservice.repository.ProjectRepository;
import com.taskservice.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    private static final String TASK_TOPIC = "task-events";

    /* =========================================================
       CREATE TASK
    ========================================================= */
    public ApiResponse<Task> createTask(CreateTaskRequest request) {
        try {
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return new ApiResponse<>("01", "Task title is required", HttpStatus.BAD_REQUEST, null);
            }

            Task task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .dueDate(request.getDueDate())
                    .assignedId(request.getAssigneeId())    // Fixed: now UUID (was Long)
                    .createdBy(request.getCreatedBy())
                    .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : Instant.now())
                    .updatedAt(Instant.now())
                    .personal(request.getPersonal() != null ? request.getPersonal() : true)
                    .taskStatus(request.getTaskStatus() != null ? request.getTaskStatus() : Task.TaskStatus.TODO)
                    .build();

            if (!task.isPersonal()) {
                if (request.getProjectId() == null) {
                    return new ApiResponse<>("02", "Project ID required for non-personal tasks", HttpStatus.BAD_REQUEST, null);
                }

                Project project = projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found"));

                task.setProject(project);
            }

            Task savedTask = taskRepository.save(task);

            kafkaTemplate.send(TASK_TOPIC, TaskEvent.builder()
                    .taskId(savedTask.getId())
                    .type("CREATED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Task created successfully", HttpStatus.CREATED, savedTask);

        } catch (DataAccessException e) {
            log.error("CREATE TASK DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("CREATE TASK KAFKA ERROR", e);
            return new ApiResponse<>("97", "Task created but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("CREATE TASK ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Task> getTaskById(UUID id) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            return new ApiResponse<>("00", "Success", HttpStatus.OK, task);

        } catch (RuntimeException e) {
            log.warn("GET TASK NOT FOUND id={}", id);
            return new ApiResponse<>("04", e.getMessage(), HttpStatus.NOT_FOUND, null);

        } catch (Exception e) {
            log.error("GET TASK ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<List<Task>> getAllTasks() {
        try {
            return new ApiResponse<>("00", "Success", HttpStatus.OK, taskRepository.findAll());

        } catch (Exception e) {
            log.error("GET ALL TASKS ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<List<Task>> getTasksByAssigneeId(UUID assignedId) {
        try {
            return new ApiResponse<>("00", "Success", HttpStatus.OK,
                    taskRepository.findByAssignedId(assignedId));

        } catch (Exception e) {
            log.error("GET TASKS BY ASSIGNEE ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<List<Task>> getTasksByProjectId(UUID projectId) {
        try {
            return new ApiResponse<>("00", "Success", HttpStatus.OK,
                    taskRepository.findByProjectId(projectId));

        } catch (Exception e) {
            log.error("GET TASKS BY PROJECT ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Task> updateTask(UpdateTaskRequest request) {
        try {
            Task task = taskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            Optional.ofNullable(request.getTitle()).ifPresent(task::setTitle);
            Optional.ofNullable(request.getDescription()).ifPresent(task::setDescription);
            Optional.ofNullable(request.getDueDate()).ifPresent(task::setDueDate);
            Optional.ofNullable(request.getAssignedId()).ifPresent(task::setAssignedId);
            Optional.ofNullable(request.getTaskStatus()).ifPresent(task::setTaskStatus);

            task.setUpdatedAt(Instant.now());

            Task updated = taskRepository.save(task);

            kafkaTemplate.send(TASK_TOPIC, TaskEvent.builder()
                    .taskId(updated.getId())
                    .type("UPDATED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Updated", HttpStatus.OK, updated);

        } catch (RuntimeException e) {
            log.warn("UPDATE TASK NOT FOUND", e);
            return new ApiResponse<>("04", e.getMessage(), HttpStatus.NOT_FOUND, null);

        } catch (Exception e) {
            log.error("UPDATE TASK ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<String> deleteTask(UUID id) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            taskRepository.delete(task);

            kafkaTemplate.send(TASK_TOPIC, TaskEvent.builder()
                    .taskId(task.getId())
                    .type("DELETED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Deleted", HttpStatus.OK, null);

        } catch (RuntimeException e) {
            log.warn("DELETE TASK NOT FOUND id={}", id);
            return new ApiResponse<>("04", e.getMessage(), HttpStatus.NOT_FOUND, null);

        } catch (Exception e) {
            log.error("DELETE TASK ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    @Transactional
    public ApiResponse<Integer> bulkUpdateStatus(List<UUID> taskIds, Task.TaskStatus status) {
        try {
            int count = taskRepository.bulkUpdateStatus(taskIds, status);
            return new ApiResponse<>("00", count + " tasks updated", HttpStatus.OK, count);

        } catch (DataAccessException e) {
            log.error("BULK UPDATE DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("BULK UPDATE ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    @Transactional
    public ApiResponse<Integer> bulkReassignTasks(UUID oldId, UUID newId) {
        try {
            int count = taskRepository.bulkReassignTasks(oldId, newId);
            return new ApiResponse<>("00", count + " tasks reassigned", HttpStatus.OK, count);

        } catch (DataAccessException e) {
            log.error("BULK REASSIGN DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("BULK REASSIGN ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    /* =========================================================
       BULK DELETE BY PROJECT
    ========================================================= */
    @Transactional
    public ApiResponse<Integer> bulkDeleteByProject(UUID projectId) {
        try {
            int count = taskRepository.bulkDeleteByProject(projectId);
            return new ApiResponse<>("00", count + " tasks deleted", HttpStatus.OK, count);

        } catch (DataAccessException e) {
            log.error("BULK DELETE DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("BULK DELETE ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }
}
