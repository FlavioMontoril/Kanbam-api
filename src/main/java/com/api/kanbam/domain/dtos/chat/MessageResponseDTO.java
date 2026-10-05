package com.api.kanbam.domain.dtos.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.kanbam.domain.entities.chat.Message;

public record MessageResponseDTO(
    UUID id,
    UUID roomId,
    Long senderId,
    String content,
    LocalDateTime timestamp
) {
    public static MessageResponseDTO fromEntity(Message message) {
        return new MessageResponseDTO(
            message.getId(),
            message.getRoom().getId(),
            message.getSenderId(),
            message.getContent(),
            message.getTimestamp()
        );
    }
}