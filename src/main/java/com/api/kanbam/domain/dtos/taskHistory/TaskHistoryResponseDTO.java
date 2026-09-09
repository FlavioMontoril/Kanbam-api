package com.api.kanbam.domain.dtos.taskHistory;

import com.api.kanbam.domain.entities.TaskHistory;
import com.api.kanbam.domain.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskHistoryResponseDTO(
        UUID id,
        TaskStatus previousStatus,
        TaskStatus currentStatus,
        LocalDateTime movedAt
) {
    public  TaskHistoryResponseDTO(TaskHistory taskHistory){
        this(
                taskHistory.getId(),
                taskHistory.getPreviousStatus(),
                taskHistory.getCurrentStatus(),
                taskHistory.getMovedAt()
        );
    }
}
