package com.api.kanbam.domain.dtos.chat;

import java.util.List;

import com.api.kanbam.domain.entities.chat.ChatParticipant;

public record RoomCreatedEventDTO(
        List<ChatParticipant> participants,
        ChatRoomResponseDTO roomResponseDTO) {

}
