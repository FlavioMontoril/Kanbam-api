package com.api.kanbam.domain.dtos.task;

import com.api.kanbam.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusDTO(
        @NotNull(message = "O novo status é obrigatório")
        TaskStatus status
) {
}
