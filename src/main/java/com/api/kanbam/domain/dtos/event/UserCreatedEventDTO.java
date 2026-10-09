package com.api.kanbam.domain.dtos.event;

import java.util.UUID;

public record UserCreatedEventDTO(
        UUID id,
        String name,
        String email,
        String role
) {
}
