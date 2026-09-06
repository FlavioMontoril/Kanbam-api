package com.api.kanbam.domain.dtos.task;

public record TasksCountDTO(
        long open,
        long done,
        long cancelled,
        long in_progress,
        long under_review,
        long total
) {
}
