package com.taskservice.service;

import com.taskservice.dto.request.*;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.event.ProjectEvent;
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

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, ProjectEvent> projectKafka;
    private final KafkaTemplate<String, TaskEvent> taskKafka;

    private static final String PROJECT_TOPIC = "project-events";
    private static final String TASK_TOPIC = "task-events";


    public ApiResponse<Project> createProject(CreateProjectRequest request) {
        try {
            Project project = Project.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : Instant.now())
                    .createdBy(request.getCreatedBy())
                    .ownerId(request.getOwnerId())
                    .team(request.getTeamId())
                    .status(request.getProjectStatus() != null ? request.getProjectStatus() : Project.ProjectStatus.INACTIVE)
                    .priority(request.getPriority() != null ? request.getPriority() : Project.Priority.MEDIUM)
                    .build();

            Project saved = projectRepository.save(project);

            projectKafka.send(PROJECT_TOPIC, ProjectEvent.builder()
                    .projectId(saved.getId())
                    .type("CREATED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Project created", HttpStatus.CREATED, saved);

        } catch (DataAccessException e) {
            log.error("CREATE PROJECT DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("CREATE PROJECT KAFKA ERROR", e);
            return new ApiResponse<>("97", "Project created but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("CREATE PROJECT ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Project> getProjectById(UUID id) {
        try {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            return new ApiResponse<>("00", "Success", HttpStatus.OK, project);

        } catch (RuntimeException e) {
            log.warn("GET PROJECT NOT FOUND id={}", id);
            return new ApiResponse<>("04", e.getMessage(), HttpStatus.NOT_FOUND, null);

        } catch (Exception e) {
            log.error("GET PROJECT ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<List<Project>> getAllProjects() {
        try {
            return new ApiResponse<>("00", "Success", HttpStatus.OK, projectRepository.findAll());

        } catch (Exception e) {
            log.error("GET ALL PROJECTS ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ApiResponse<Project> addTaskToProject(AddTaskRequest request) {
        try {
            UUID projectId = request.getProjectId();

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            List<Task> existingTasks = taskRepository.findByProjectId(projectId);

            Set<String> existingTitles = new HashSet<>();
            for (Task t : existingTasks) {
                existingTitles.add(t.getTitle().toLowerCase());
            }

            List<CreateTaskRequest> requestedTasks = request.getTasks();
            if (requestedTasks == null || requestedTasks.isEmpty()) {
                return new ApiResponse<>("03", "No tasks provided", HttpStatus.BAD_REQUEST, null);
            }

            List<Task> newTasks = new ArrayList<>();

            for (CreateTaskRequest dto : requestedTasks) {
                if (dto.getTitle() == null || dto.getTitle().isBlank()) continue;

                String key = dto.getTitle().toLowerCase();
                if (existingTitles.contains(key)) continue;

                Task task = Task.builder()
                        .title(dto.getTitle())
                        .description(dto.getDescription())
                        .dueDate(dto.getDueDate())
                        .assignedId(dto.getAssigneeId())
                        .createdBy(dto.getCreatedBy())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .project(project)
                        .personal(false)
                        .taskStatus(Task.TaskStatus.TODO)
                        .build();

                newTasks.add(task);
                existingTitles.add(key);
            }

            if (newTasks.isEmpty()) {
                return new ApiResponse<>("01", "No new tasks to add", HttpStatus.OK, project);
            }

            taskRepository.saveAll(newTasks);

            for (Task t : newTasks) {
                taskKafka.send(TASK_TOPIC, TaskEvent.builder()
                        .taskId(t.getId())
                        .type("CREATED")
                        .timestamp(Instant.now())
                        .build());
            }

            projectKafka.send(PROJECT_TOPIC, ProjectEvent.builder()
                    .projectId(projectId)
                    .type("TASKS_ADDED")
                    .timestamp(Instant.now())
                    .build());

            Project updatedProject = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            return new ApiResponse<>("00", "Tasks added successfully", HttpStatus.CREATED, updatedProject);

        } catch (DataAccessException e) {
            log.error("ADD TASK DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("ADD TASK KAFKA ERROR", e);
            return new ApiResponse<>("97", "Tasks added but event publish failed: " + e.getMessage(), HttpStatus.MULTI_STATUS, null);

        } catch (Exception e) {
            log.error("ADD TASK ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Project> removeTaskFromProject(RemoveTaskRequest request) {
        try {
            UUID projectId = request.getProjectId();

            projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            List<Task> tasks = taskRepository.findByProjectId(projectId);

            if (tasks.isEmpty()) {
                return new ApiResponse<>("04", "No tasks found for project", HttpStatus.NOT_FOUND, null);
            }

            List<RemoveTaskRequest.TaskRef> requestedTasks = request.getTasks();
            if (requestedTasks == null || requestedTasks.isEmpty()) {
                return new ApiResponse<>("03", "No task IDs provided", HttpStatus.BAD_REQUEST, null);
            }

            Set<UUID> idsToRemove = requestedTasks.stream()
                    .filter(t -> t.getId() != null)
                    .map(RemoveTaskRequest.TaskRef::getId)
                    .collect(Collectors.toSet());

            if (idsToRemove.isEmpty()) {
                return new ApiResponse<>("03", "No valid task IDs provided", HttpStatus.BAD_REQUEST, null);
            }

            List<Task> toUpdate = tasks.stream()
                    .filter(t -> idsToRemove.contains(t.getId()))
                    .peek(t -> t.setProject(null))
                    .collect(Collectors.toList());

            if (toUpdate.isEmpty()) {
                return new ApiResponse<>("04", "No matching tasks found in project", HttpStatus.NOT_FOUND, null);
            }

            taskRepository.saveAll(toUpdate);

            // Send per-task Kafka events
            for (Task t : toUpdate) {
                taskKafka.send(TASK_TOPIC, TaskEvent.builder()
                        .taskId(t.getId())
                        .type("REMOVED_FROM_PROJECT")
                        .timestamp(Instant.now())
                        .build());
            }

            projectKafka.send(PROJECT_TOPIC, ProjectEvent.builder()
                    .projectId(projectId)
                    .type("TASKS_REMOVED")
                    .timestamp(Instant.now())
                    .build());

            // Return updated project
            Project updatedProject = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            return new ApiResponse<>("00", toUpdate.size() + " tasks removed successfully", HttpStatus.OK, updatedProject);

        } catch (DataAccessException e) {
            log.error("REMOVE TASK DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("REMOVE TASK KAFKA ERROR", e);
            return new ApiResponse<>("97", "Tasks removed but event publish failed: " + e.getMessage(), HttpStatus.MULTI_STATUS, null);

        } catch (Exception e) {
            log.error("REMOVE TASK ERROR", e);
            return new ApiResponse<>("99", "Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Project> updateProject(UpdateProjectRequest request) {
        try {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            Optional.ofNullable(request.getName()).ifPresent(project::setName);
            Optional.ofNullable(request.getDescription()).ifPresent(project::setDescription);
            Optional.ofNullable(request.getStatus()).ifPresent(project::setStatus);
            Optional.ofNullable(request.getPriority()).ifPresent(project::setPriority);

            Project updated = projectRepository.save(project);

            projectKafka.send(PROJECT_TOPIC, ProjectEvent.builder()
                    .projectId(updated.getId())
                    .type("UPDATED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Updated", HttpStatus.OK, updated);

        } catch (RuntimeException e) {
            log.warn("UPDATE PROJECT NOT FOUND", e);
            return new ApiResponse<>("04", e.getMessage(), HttpStatus.NOT_FOUND, null);

        } catch (Exception e) {
            log.error("UPDATE PROJECT ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    /* =========================================================
       DELETE PROJECT
    ========================================================= */
    public ApiResponse<String> deleteProject(UUID id) {
        try {
            if (!projectRepository.existsById(id)) {
                return new ApiResponse<>("04", "Project not found", HttpStatus.NOT_FOUND, null);
            }

            projectRepository.deleteById(id);

            projectKafka.send(PROJECT_TOPIC, ProjectEvent.builder()
                    .projectId(id)
                    .type("DELETED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>("00", "Deleted", HttpStatus.OK, null);

        } catch (DataAccessException e) {
            log.error("DELETE PROJECT DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("DELETE PROJECT ERROR", e);
            return new ApiResponse<>("99", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }
}
