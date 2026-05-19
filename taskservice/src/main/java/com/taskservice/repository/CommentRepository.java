package com.taskservice.repository;

import com.taskservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskId(UUID taskId);

    List<Comment> findByTaskIdAndDeletedFalse(UUID taskId);

    Optional<Comment> findByIdAndDeletedFalse(UUID id);
}

