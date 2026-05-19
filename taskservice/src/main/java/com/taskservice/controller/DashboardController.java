package com.taskservice.controller;

import com.taskservice.dto.response.ApiResponse;
import com.taskservice.model.Task;
import com.taskservice.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TaskService taskService;

    // Get user's assigned tasks
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<Task>>> getAssignedTasks(@RequestParam UUID userId) {
        ApiResponse<List<Task>> tasks = taskService.getTasksByAssigneeId(userId);
        return ResponseEntity.ok(tasks);
    }

    // Get upcoming deadlines (tasks due within next 7 days)
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<Task>>> getUpcomingDeadlines(@RequestParam UUID userId) {
        // This would require a custom query, for now return all assigned tasks
        ApiResponse<List<Task>> tasks = taskService.getTasksByAssigneeId(userId);
        return ResponseEntity.ok(tasks);
    }

    // Get recent activity (recently updated tasks)
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Task>>>getRecentActivity(@RequestParam UUID userId) {
        ApiResponse<List<Task>> tasks = taskService.getTasksByAssigneeId(userId);
        return ResponseEntity.ok(tasks);
    }
}
