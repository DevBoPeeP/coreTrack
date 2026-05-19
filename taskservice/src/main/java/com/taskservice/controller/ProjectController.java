package com.taskservice.controller;


import com.taskservice.dto.request.AddTaskRequest;
import com.taskservice.dto.request.CreateProjectRequest;
import com.taskservice.dto.request.RemoveTaskRequest;
import com.taskservice.dto.request.UpdateProjectRequest;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.model.Project;
import com.taskservice.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {


    @Autowired
    private ProjectService projectService;

    @PostMapping()
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestBody CreateProjectRequest request) {
        ApiResponse<Project> response = projectService.createProject(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable UUID id) {
        ApiResponse<Project> response = projectService.getProjectById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    @GetMapping()
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        ApiResponse<List<Project>> response = projectService.getAllProjects();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> updateProject(@PathVariable UUID id, @RequestBody UpdateProjectRequest request) {
        request.setProjectId(id);
        ApiResponse<Project> response = projectService.updateProject(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/{id}/add")
    public ResponseEntity<ApiResponse<Project>> addTaskToProject(@PathVariable UUID id, @RequestBody AddTaskRequest request) {
        request.setProjectId(id);
        ApiResponse<Project> response = projectService.addTaskToProject(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Project>> removeTaskFromProject(@PathVariable UUID id, @RequestBody RemoveTaskRequest request) {
        request.setProjectId(id);
        ApiResponse<Project> response = projectService.removeTaskFromProject(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
