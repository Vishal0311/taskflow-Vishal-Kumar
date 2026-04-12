package com.taskflow.service;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;      // ADD THIS
    private final TaskService taskService;            // ADD THIS

    public Map<String, Object> getAllProjects(User currentUser, int page, int limit) {
        log.info("User {} fetching projects - page: {}, limit: {}",
                currentUser.getEmail(), page, limit);
        Pageable pageable = PageRequest.of(page, limit);
        Page<Project> projectPage = projectRepository.findAllAccessibleByUser(currentUser, pageable);

        return Map.of(
                "data", projectPage.getContent().stream()
                        .map(p -> toResponse(p, false))
                        .toList(),
                "page", page,
                "limit", limit,
                "total", projectPage.getTotalElements(),
                "totalPages", projectPage.getTotalPages()
        );
    }

    public ProjectResponse getProjectById(UUID id, User currentUser) {
        log.info("User {} fetching project: {}", currentUser.getEmail(), id);
        Project project = findProjectOrThrow(id);
        checkAccess(project, currentUser);
        return toResponse(project, true);            // true = include tasks
    }

    public ProjectResponse createProject(ProjectRequest request, User currentUser) {
        log.info("User {} creating project: {}", currentUser.getEmail(), request.getName());
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();
        log.info("Project created with id: {}", project.getId());
        return toResponse(projectRepository.save(project), false);
    }

    public ProjectResponse updateProject(UUID id, ProjectRequest request, User currentUser) {
        log.info("User {} updating project: {}", currentUser.getEmail(), id);
        Project project = findProjectOrThrow(id);
        checkOwner(project, currentUser);

        project.setName(request.getName());
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        log.info("Project {} updated successfully", id);
        return toResponse(projectRepository.save(project), false);
    }

    public void deleteProject(UUID id, User currentUser) {
        log.info("User {} deleting project: {}", currentUser.getEmail(), id);
        Project project = findProjectOrThrow(id);
        checkOwner(project, currentUser);
        projectRepository.delete(project);
        log.info("Project {} deleted successfully", id);
    }

    // ── helpers ──────────────────────────────────────────

    private Project findProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    private void checkOwner(Project project, User currentUser) {
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted unauthorized access to project: {}",
                    currentUser.getEmail(), project.getId());
            throw new RuntimeException("FORBIDDEN");
        }
    }

    private void checkAccess(Project project, User currentUser) {
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner) {
            throw new RuntimeException("FORBIDDEN");
        }
    }

    public ProjectResponse toResponse(Project project, boolean includeTasks) {
        List<TaskResponse> tasks = null;
        if (includeTasks) {
            tasks = taskRepository.findByProjectId(project.getId())
                    .stream()
                    .map(taskService::toResponse)
                    .toList();
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .ownerName(project.getOwner().getName())
                .createdAt(project.getCreatedAt())
                .tasks(tasks)
                .build();
    }

    public Map<String, Object> getProjectStats(UUID id, User currentUser) {
        log.info("User {} fetching stats for project: {}", currentUser.getEmail(), id);
        findProjectOrThrow(id);
        List<Task> tasks = taskRepository.findByProjectId(id);

        Map<String, Long> byStatus = tasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Long> byAssignee = tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getAssignee().getName(),
                        java.util.stream.Collectors.counting()
                ));

        return Map.of(
                "totalTasks", tasks.size(),
                "byStatus", byStatus,
                "byAssignee", byAssignee
        );
    }
}