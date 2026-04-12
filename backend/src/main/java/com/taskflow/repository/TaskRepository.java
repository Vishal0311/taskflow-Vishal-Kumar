package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    Page<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status, Pageable pageable);

    Page<Task> findByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId, Pageable pageable);

    Page<Task> findByProjectIdAndStatusAndAssigneeId(
            UUID projectId, TaskStatus status, UUID assigneeId, Pageable pageable
    );

    boolean existsByIdAndProjectOwnerId(UUID taskId, UUID ownerId);
}