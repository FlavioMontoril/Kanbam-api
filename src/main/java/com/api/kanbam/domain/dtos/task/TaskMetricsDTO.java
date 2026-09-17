package com.api.kanbam.domain.dtos.task;

public record TaskMetricsDTO (
    int month,
    long totalTasks,
    long openTasks,
    long doneTasks,
    long canceledTasks
){}
