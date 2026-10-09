package com.api.kanbam.domain.dtos.chat;

import java.util.List;
import java.util.UUID;

import com.api.kanbam.domain.entities.chat.ChatParticipant;

public record RoomCreatedEventDTO(
        UUID creatorId,
        List<ChatParticipant> participants,
        ChatRoomResponseDTO roomResponseDTO) {

}
