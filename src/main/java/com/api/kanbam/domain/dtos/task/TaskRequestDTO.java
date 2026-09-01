package com.api.kanbam.domain.dtos.task;

public record TaskRequestDTO(
        String code,
        String title,
        String description,
        String reporter,
        String assignee
) {
}
