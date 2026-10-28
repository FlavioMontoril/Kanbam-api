package com.api.kanbam.domain.dtos.chat;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageDTO(
    @NotNull(message = "O ID da sala é obrigatório")
    UUID roomId,
    @NotBlank(message = "O conteúdo da mensagem não pode estar vazio")
    String content
) {}