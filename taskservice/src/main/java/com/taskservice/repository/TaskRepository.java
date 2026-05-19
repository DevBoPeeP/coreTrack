package com.taskservice.repository;

import com.taskservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

 // Retrieve tasks assigned to a specific user
 List<Task> findByAssignedId(UUID assignedId);

 // Retrieve tasks by projectId (UUID-based)
 List<Task> findByProjectId(UUID projectId);

 // Retrieve tasks created by a specific user
 List<Task> findByCreatedBy(String createdBy);

 // Retrieve tasks by status
 List<Task> findByTaskStatus(Task.TaskStatus taskStatus);

 // Retrieve tasks due before a given date
 List<Task> findByDueDateBefore(LocalDate dueDate);

    /* =========================================================
       BULK OPERATIONS
    ========================================================= */

 @Modifying(clearAutomatically = true, flushAutomatically = true)
 @Query("UPDATE Task t SET t.taskStatus = :status WHERE t.id IN :taskIds")
 int bulkUpdateStatus(@Param("taskIds") List<UUID> taskIds,
                      @Param("status") Task.TaskStatus status);

 @Modifying(clearAutomatically = true, flushAutomatically = true)
 @Query("UPDATE Task t SET t.assignedId = :newAssigneeId WHERE t.assignedId = :oldAssigneeId")
 int bulkReassignTasks(@Param("oldAssigneeId") UUID oldAssigneeId,
                       @Param("newAssigneeId") UUID newAssigneeId);

 @Modifying(clearAutomatically = true, flushAutomatically = true)
 @Query("DELETE FROM Task t WHERE t.project = :projectId")
 int bulkDeleteByProject(@Param("projectId") UUID projectId);
}