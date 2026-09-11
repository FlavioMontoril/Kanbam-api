package com.api.kanbam.domain.dtos.task;

import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String code,
        String title,
        String description,
        TaskStatus status,
        String reporter,
        String assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean archived,
        UUID userId
) {
    public  TaskResponseDTO(Task task){
        this(
                task.getId(),
                task.getCode(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getReporter(),
                task.getAssignee(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.isArchived(),
                task.getUser() != null ? task.getUser().getId() : null);
    }
}
