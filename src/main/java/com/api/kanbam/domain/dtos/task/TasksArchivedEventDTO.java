package com.api.kanbam.domain.dtos.task;

import java.util.List;
import java.util.UUID;

public record TasksArchivedEventDTO(List<TaskResponseDTO> archivedTasks,  UUID triggeredId) {
}
