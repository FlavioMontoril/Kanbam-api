package com.api.kanbam.domain.dtos.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.kanbam.domain.entities.chat.ChatParticipant;
import com.api.kanbam.domain.enums.RoleMember;

public record ChatParticipantResponseDTO(
    UUID id,
    Long userId,
    RoleMember role,
    LocalDateTime joinedAt
) {
    public static ChatParticipantResponseDTO fromEntity(ChatParticipant participant) {
        return new ChatParticipantResponseDTO(
            participant.getId(),
            participant.getUserId(),
            participant.getRole(),
            participant.getJoinedAt()
        );
    }
}