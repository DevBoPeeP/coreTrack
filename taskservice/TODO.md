# TODO List for Task Service Implementation

## 1. Fix Models

- [x] Update task.java: Change id to Long, dueDate to LocalDate, assigneeId to Long, createdBy to Long
- [x] Update project.java if needed
- [x] Update team.java if needed

## 2. Create Missing Repositories

- [x] Create teamRepository.java

## 3. Implement Services

- [x] Implement taskService.java with CRUD, assignment, status updates, bulk ops
- [x] Create projectService.java with CRUD, add tasks
- [x] Create teamService.java with CRUD

## 4. Create Controllers

- [x] Create TaskController.java for task CRUD, bulk ops
- [x] Create ProjectController.java for project management
- [x] Create TeamController.java for team management
- [ ] Create DashboardController.java for dashboard endpoints

## 5. Update DTOs

- [x] Update createTaskRequest.java to match model
- [ ] Update other DTOs if needed
- [ ] Create additional DTOs for responses, updates

## 6. Implement Events and Exceptions

- [x] Implement taskEvent.java for event publishing
- [x] Implement globalExceptionHandler.java

## 7. Add Security

- [x] Create JWT configuration and filters

## 8. Configuration

- [x] Update application.properties for DB, JWT, etc.

## 9. Microservice Features

- [ ] Add OpenFeign client for User Service integration
- [ ] Add event publishing for task changes

## 10. Docker and Kubernetes

- [x] Create Dockerfile
- [x] Create Kubernetes manifests (deployment, service, configmap, secret)

## 11. Testing

- [ ] Test endpoints
- [ ] Run the application
