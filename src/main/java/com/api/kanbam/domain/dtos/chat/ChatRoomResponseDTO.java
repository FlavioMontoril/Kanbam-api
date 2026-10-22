package com.api.kanbam.domain.dtos.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.api.kanbam.domain.entities.chat.ChatRoom;
import com.api.kanbam.domain.enums.ChatType;

public record ChatRoomResponseDTO(
    UUID id,
    String name,
    ChatType type,
    LocalDateTime createdAt,
    List<ChatParticipantResponseDTO> participants
) {
    public static ChatRoomResponseDTO fromEntity(ChatRoom room) {
        return new ChatRoomResponseDTO(
            room.getId(),
            room.getName(),
            room.getType(),
            room.getCreatedAt(),
            room.getParticipants().stream()
                .map(ChatParticipantResponseDTO::fromEntity)
                .toList()
        );
    }
}