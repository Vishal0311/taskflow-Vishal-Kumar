package com.taskflow.service;

import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.model.*;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getTasks(UUID projectId, TaskStatus status,
                                        UUID assigneeId, int page, int limit,
                                        User currentUser) {
        log.info("User {} fetching tasks for project: {} - page: {}, limit: {}",
                currentUser.getEmail(), projectId, page, limit);
        findProjectOrThrow(projectId);
        Pageable pageable = PageRequest.of(page, limit);

        Page<Task> taskPage;

        if (status != null && assigneeId != null) {
            taskPage = taskRepository.findByProjectIdAndStatusAndAssigneeId(
                    projectId, status, assigneeId, pageable);
        } else if (status != null) {
            taskPage = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        } else if (assigneeId != null) {
            taskPage = taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId, pageable);
        } else {
            taskPage = taskRepository.findByProjectId(projectId, pageable);
        }

        return Map.of(
                "data", taskPage.getContent().stream().map(this::toResponse).toList(),
                "page", page,
                "limit", limit,
                "total", taskPage.getTotalElements(),
                "totalPages", taskPage.getTotalPages()
        );
    }

    public TaskResponse createTask(UUID projectId, TaskRequest request, User currentUser) {
        log.info("User {} creating task in project: {}", currentUser.getEmail(), projectId);
        Project project = findProjectOrThrow(projectId);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.todo)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.medium)
                .project(project)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .createdAt(LocalDateTime.now())    // ADD THIS
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Task created with id: {}", task.getId());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(UUID taskId, TaskRequest request, User currentUser) {
        log.info("User {} updating task: {}", currentUser.getEmail(), taskId);

        Task task = findTaskOrThrow(taskId);
        checkTaskAccess(task, currentUser);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }
        log.info("Task {} updated successfully", taskId);

        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(UUID taskId, User currentUser) {
        log.info("User {} deleting task: {}", currentUser.getEmail(), taskId);
        Task task = findTaskOrThrow(taskId);
        checkTaskAccess(task, currentUser);
        taskRepository.delete(task);
        log.info("Task {} deleted successfully", taskId);

    }

    // ── helpers ──────────────────────────────────────────

    private Project findProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private void checkTaskAccess(Task task, User currentUser) {
        boolean isProjectOwner = task.getProject().getOwner()
                .getId().equals(currentUser.getId());
        if (!isProjectOwner) {
            log.warn("User {} attempted unauthorized access to task: {}",
                    currentUser.getEmail(), task.getId());
            throw new RuntimeException("FORBIDDEN");
        }
    }

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}