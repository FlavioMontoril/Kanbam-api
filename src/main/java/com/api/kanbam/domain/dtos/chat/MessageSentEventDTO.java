package com.api.kanbam.domain.dtos.chat;

import com.api.kanbam.domain.entities.chat.ChatParticipant;

import java.util.List;
import java.util.UUID;

public record MessageSentEventDTO(
    UUID roomId,
    List<ChatParticipant> roomParticipants,
    MessageResponseDTO messageResponseDTO
) {
    
}
