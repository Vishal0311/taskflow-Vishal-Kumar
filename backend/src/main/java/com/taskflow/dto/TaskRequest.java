package com.taskflow.dto;

import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private UUID assigneeId;

    private LocalDate dueDate;
}