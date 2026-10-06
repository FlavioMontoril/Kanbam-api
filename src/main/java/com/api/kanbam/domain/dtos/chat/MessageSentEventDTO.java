package com.api.kanbam.domain.dtos.chat;

import java.util.UUID;

public record MessageSentEventDTO(
    UUID roomId,
    MessageResponseDTO messageResponseDTO
) {
    
}
