package com.api.kanbam.domain.dtos.chat;

import java.util.List;
import com.api.kanbam.domain.enums.ChatType;

import jakarta.validation.constraints.NotNull;

public record CreateChatRoomDTO(
    String name, // Opcional para DIRECT, obrigatório se for GROUP
    @NotNull(message = "O tipo de chat é obrigatório")
    ChatType type,
    @NotNull(message = "A lista de participantes não pode ser nula")
    List<Long> targetUserIds // IDs dos usuários a serem adicionados na sala
) {}