package com.api.kanbam.domain.dtos.task;

import java.util.List;

public record TasksArchivedEventDTO(List<TaskResponseDTO> archivedTasks) {
}
