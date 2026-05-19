package com.taskservice.controller;


import com.taskservice.dto.request.*;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.model.Team;
import com.taskservice.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping()
    public ResponseEntity <ApiResponse<Team>> createTeam(@RequestBody CreateTeamRequest request) {
        ApiResponse<Team> response = teamService.createTeam(request);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }

    @GetMapping("/{id}")
    public ResponseEntity <ApiResponse<Team>> getTeamById(@PathVariable UUID id) {
        ApiResponse<Team> response = teamService.getTeamById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }

    @GetMapping()
    public ResponseEntity <ApiResponse<java.util.List<Team>>> getAllTeams() {
        ApiResponse<java.util.List<Team>> response = teamService.getAllTeams();
        return new ResponseEntity<>(response, response.getHttpStatus());

    }

    @PutMapping("/{id}/add")
    public  ResponseEntity <ApiResponse<Team>> addUserToTeam(@PathVariable UUID id, @RequestBody AddUserRequest request) {
        request.setTeamId(id);
        ApiResponse<Team> response = teamService.addUserToTeam(request);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Team>> removeUserFromTeam(@PathVariable UUID id, @RequestBody RemoveUserRequest request) {
        request.setTeamId(id);
        ApiResponse<Team> response = teamService.removeUserFromTeam(request);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }


    @PutMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<Team>> addProjectToTeam(@PathVariable UUID id, @RequestBody AddProjectRequest request) {
        request.setTeamId(id);
        ApiResponse<Team> response = teamService.addProjectToTeam(request);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }

    @DeleteMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<Team>> removeProjectFromTeam(@PathVariable UUID id, @RequestBody RemoveProjectRequest request) {
        request.setTeamId(id);
        ApiResponse<Team> response = teamService.removeProjectFromTeam(request);
        return new ResponseEntity<>(response, response.getHttpStatus());

    }
}
