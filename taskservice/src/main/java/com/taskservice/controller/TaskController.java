package com.taskservice.controller;

import com.taskservice.dto.request.CreateTaskRequest;
import com.taskservice.dto.request.UpdateTaskRequest;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.model.Task;
import com.taskservice.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


     // Create a new task

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody CreateTaskRequest request) {
        ApiResponse<Task> response = taskService.createTask(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


     //Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable UUID id) {
        ApiResponse<Task> response = taskService.getTaskById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


     //Get all tasks

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        ApiResponse<List<Task>> response = taskService.getAllTasks();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


     // Update task

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable UUID id, @RequestBody UpdateTaskRequest request) {
        request.setTaskId(id);
        ApiResponse<Task> response = taskService.updateTask(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


     // Delete task by ID

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable UUID id) {
        ApiResponse<String> response = taskService.deleteTask(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    // Bulk update status for multiple tasks
    @PutMapping("/bulk/status")
    public ResponseEntity<ApiResponse<Integer>> bulkUpdateStatus(
            @RequestParam List<UUID> taskIds,
            @RequestParam Task.TaskStatus status) {
        ApiResponse<Integer> response = taskService.bulkUpdateStatus(taskIds, status);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    //  Bulk reassign tasks from one assignee to another

    @PutMapping("/bulk/reassign")
    public ResponseEntity<ApiResponse<Integer>> bulkReassignTasks(
            @RequestParam UUID oldAssigneeId,
            @RequestParam UUID newAssigneeId) {
        ApiResponse<Integer> response = taskService.bulkReassignTasks(oldAssigneeId, newAssigneeId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


     //Bulk delete all tasks under a project

    @DeleteMapping("/bulk/project/{projectId}")
    public ResponseEntity<ApiResponse<Integer>> bulkDeleteByProject(@PathVariable UUID projectId) {
        ApiResponse<Integer> response = taskService.bulkDeleteByProject(projectId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Get tasks by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByStatus(@PathVariable String status) {
        List<Task> filteredTasks = taskService.getAllTasks()
                .getData()
                .stream()
                .filter(t -> t.getTaskStatus().name().equalsIgnoreCase(status))
                .toList();

        ApiResponse<List<Task>> apiResponse = new ApiResponse<>(
                "00",
                "Tasks filtered by status: " + status,
                org.springframework.http.HttpStatus.OK,
                filteredTasks
        );

        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }
}
