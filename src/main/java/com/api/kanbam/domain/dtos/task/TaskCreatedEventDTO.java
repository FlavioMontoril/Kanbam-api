package com.api.kanbam.domain.dtos.task;

import java.util.UUID;

public record TaskCreatedEventDTO(TaskResponseDTO createdTask, UUID triggeredId) {
}
