package com.taskservice.service;

import com.taskservice.dto.request.*;
import com.taskservice.dto.response.ApiResponse;
import com.taskservice.event.TeamEvent;
import com.taskservice.model.Project;
import com.taskservice.model.Team;
import com.taskservice.repository.ProjectRepository;
import com.taskservice.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final KafkaTemplate<String, TeamEvent> teamKafka;  // Replaced: ApplicationEventPublisher → KafkaTemplate

    private static final String TEAM_TOPIC = "team-events";


    public ApiResponse<Team> createTeam(CreateTeamRequest request) {
        try {
            if (request.getCreatedAt() == null) {
                request.setCreatedAt(Instant.now());
            }

            Team team = Team.builder()
                    .name(request.getName())
                    .createdAt(request.getCreatedAt())
                    .memberIds(
                            request.getMemberIds() != null
                                    ? new HashSet<>(request.getMemberIds())
                                    : new HashSet<>()
                    )
                    .memberNames(
                            request.getMemberNames() != null
                                    ? new HashSet<>(request.getMemberNames())
                                    : new HashSet<>()
                    )
                    .projects(new ArrayList<>())
                    .build();

            Team savedTeam = teamRepository.save(team);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(savedTeam.getId())
                    .type("CREATED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Team created successfully",
                    HttpStatus.CREATED,
                    savedTeam
            );

        } catch (DataAccessException e) {
            log.error("CREATE TEAM DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("CREATE TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Team created but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("CREATE TEAM ERROR", e);
            return new ApiResponse<>("99", "Error creating team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Team> getTeamById(UUID id) {
        return teamRepository.findById(id)
                .map(team -> new ApiResponse<>(
                        "00",
                        "Team retrieved successfully",
                        HttpStatus.OK,
                        team
                ))
                .orElseGet(() -> new ApiResponse<>(
                        "01",
                        "Team not found",
                        HttpStatus.NOT_FOUND,
                        null
                ));
    }


    public ApiResponse<List<Team>> getAllTeams() {
        try {
            List<Team> teams = teamRepository.findAll();
            return new ApiResponse<>(
                    "00",
                    "Teams retrieved successfully",
                    HttpStatus.OK,
                    teams
            );
        } catch (Exception e) {
            log.error("GET ALL TEAMS ERROR", e);
            return new ApiResponse<>(
                    "99",
                    "Error retrieving teams: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }


    public ApiResponse<Team> addUserToTeam(AddUserRequest request) {
        try {
            Optional<Team> optionalTeam = teamRepository.findById(request.getTeamId());
            if (optionalTeam.isEmpty()) {
                return new ApiResponse<>(
                        "04",
                        "Team with ID " + request.getTeamId() + " not found",
                        HttpStatus.NOT_FOUND,
                        null
                );
            }

            Team team = optionalTeam.get();
            team.getMemberIds().addAll(request.getMemberIds());
            team.getMemberNames().addAll(request.getMemberNames());


            team.setUpdatedAt(Instant.now());
            Team updatedTeam = teamRepository.save(team);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(updatedTeam.getId())
                    .type("USER_ADDED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Users successfully added to Team (ID: " + request.getTeamId() + ")",
                    HttpStatus.OK,
                    updatedTeam
            );

        } catch (DataAccessException e) {
            log.error("ADD USER TO TEAM DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("ADD USER TO TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Users added but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("ADD USER TO TEAM ERROR", e);
            return new ApiResponse<>("99", "Error adding users to team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Team> removeUserFromTeam(RemoveUserRequest request) {
        try {
            Optional<Team> optionalTeam = teamRepository.findById(request.getTeamId());
            if (optionalTeam.isEmpty()) {
                return new ApiResponse<>(
                        "04",
                        "Team with ID " + request.getTeamId() + " not found",
                        HttpStatus.NOT_FOUND,
                        null
                );
            }

            Team team = optionalTeam.get();
            team.getMemberIds().removeAll(request.getMemberIds());
            team.getMemberNames().removeAll(request.getMemberNames());


            team.setUpdatedAt(Instant.now());
            Team updatedTeam = teamRepository.save(team);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(updatedTeam.getId())
                    .type("USER_REMOVED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Users successfully removed from Team (ID: " + request.getTeamId() + ")",
                    HttpStatus.OK,
                    updatedTeam
            );

        } catch (DataAccessException e) {
            log.error("REMOVE USER FROM TEAM DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("REMOVE USER FROM TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Users removed but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("REMOVE USER FROM TEAM ERROR", e);
            return new ApiResponse<>("99", "Error removing users from team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Team> addProjectToTeam(AddProjectRequest request) {
        try {
            Optional<Team> optionalTeam = teamRepository.findById(request.getTeamId());
            if (optionalTeam.isEmpty()) {
                return new ApiResponse<>(
                        "04",
                        "Team with ID " + request.getTeamId() + " not found",
                        HttpStatus.NOT_FOUND,
                        null
                );
            }

            Team team = optionalTeam.get();

            List<Project> existingProjects = projectRepository.findAllByTeamId(request.getTeamId());

            List<Project> newProjects = new ArrayList<>();

            for (AddProjectRequest.ProjectRef ref : request.getProjects()) {
                boolean exists = existingProjects.stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase(ref.getName()));

                if (!exists) {
                    Project project = projectRepository.findById(ref.getId())
                            .orElse(null);

                    if (project != null) {
                        project.setTeam(team);
                        newProjects.add(project);
                    }
                }
            }

            if (newProjects.isEmpty()) {
                return new ApiResponse<>(
                        "02",
                        "Projects already exist for this team or none were found",
                        HttpStatus.CONFLICT,
                        null
                );
            }

            team.getProjects().addAll(newProjects);



            team.setUpdatedAt(Instant.now());
            Team updatedTeam = teamRepository.save(team);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(updatedTeam.getId())
                    .type("PROJECT_ADDED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Projects successfully added to Team (ID: " + request.getTeamId() + ")",
                    HttpStatus.OK,
                    updatedTeam
            );

        } catch (DataAccessException e) {
            log.error("ADD PROJECT TO TEAM DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("ADD PROJECT TO TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Projects added but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("ADD PROJECT TO TEAM ERROR", e);
            return new ApiResponse<>("99", "Error adding projects to team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<Team> removeProjectFromTeam(RemoveProjectRequest request) {
        try {
            Optional<Team> optionalTeam = teamRepository.findById(request.getTeamId());
            if (optionalTeam.isEmpty()) {
                return new ApiResponse<>(
                        "04",
                        "Team with ID " + request.getTeamId() + " not found",
                        HttpStatus.NOT_FOUND,
                        null
                );
            }

            Team team = optionalTeam.get();
            List<Project> teamProjects = team.getProjects();
            List<Project> projectsToRemove = new ArrayList<>();

            for (RemoveProjectRequest.ProjectRef ref : request.getProjects()) {
                Optional<Project> projectToRemove = teamProjects.stream()
                        .filter(p -> {
                            if (ref.getId() != null) {
                                return Objects.equals(p.getId(), ref.getId());
                            }
                            return p.getName().equalsIgnoreCase(ref.getName());
                        })
                        .findFirst();

                if (projectToRemove.isPresent()) {
                    projectsToRemove.add(projectToRemove.get());
                } else {
                    return new ApiResponse<>(
                            "03",
                            "Project " + (ref.getName() != null ? ref.getName() : ref.getId())
                                    + " does not exist in this team",
                            HttpStatus.NOT_FOUND,
                            null
                    );
                }
            }

            for (Project project : projectsToRemove) {
                team.getProjects().remove(project);
                project.setTeam(null);
            }


            team.setUpdatedAt(Instant.now());
            Team updatedTeam = teamRepository.save(team);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(updatedTeam.getId())
                    .type("PROJECT_REMOVED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Projects successfully removed from Team (ID: " + request.getTeamId() + ")",
                    HttpStatus.OK,
                    updatedTeam
            );

        } catch (DataAccessException e) {
            log.error("REMOVE PROJECT FROM TEAM DB ERROR", e);
            return new ApiResponse<>("98", "Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (KafkaException e) {
            log.error("REMOVE PROJECT FROM TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Projects removed but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("REMOVE PROJECT FROM TEAM ERROR", e);
            return new ApiResponse<>("99", "Error removing projects from team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ApiResponse<String> deleteTeam(UUID id) {
        if (!teamRepository.existsById(id)) {
            return new ApiResponse<>(
                    "01",
                    "Team not found",
                    HttpStatus.NOT_FOUND,
                    null
            );
        }
        try {
            teamRepository.deleteById(id);

            teamKafka.send(TEAM_TOPIC, TeamEvent.builder()
                    .teamId(id)
                    .type("DELETED")
                    .timestamp(Instant.now())
                    .build());

            return new ApiResponse<>(
                    "00",
                    "Team deleted successfully",
                    HttpStatus.OK,
                    "Team with ID " + id + " deleted"
            );

        } catch (KafkaException e) {
            log.error("DELETE TEAM KAFKA ERROR", e);
            return new ApiResponse<>("97", "Team deleted but event publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);

        } catch (Exception e) {
            log.error("DELETE TEAM ERROR", e);
            return new ApiResponse<>("99", "Error deleting team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }
}