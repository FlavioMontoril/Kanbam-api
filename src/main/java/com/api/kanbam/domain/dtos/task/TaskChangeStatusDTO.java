package com.api.kanbam.domain.dtos.task;

import java.util.UUID;

public record TaskChangeStatusDTO(TaskResponseDTO changeStatus, UUID triggeredId) {
}
