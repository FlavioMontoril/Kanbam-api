package com.api.kanbam.domain.dtos.task;

import java.util.UUID;

public record TaskRequestDTO(
        String code,
        String title,
        String description,
        String reporter,
        String assignee,
        UUID userId
) {
}
