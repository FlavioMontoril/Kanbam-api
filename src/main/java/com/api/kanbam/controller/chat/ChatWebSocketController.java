package com.api.kanbam.controller.chat;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.api.kanbam.domain.dtos.chat.MessageResponseDTO;
import com.api.kanbam.domain.dtos.chat.SendMessageDTO;
import com.api.kanbam.services.chat.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void processMessage(Principal principal, @Payload SendMessageDTO dto) {
        log.info("[WebSocket] Mensagem recebida. Principal: {}, DTO: {}", principal != null ? principal.getName() : "null", dto);

        try {
            if (principal == null || principal.getName() == null) {
                log.error("[WebSocket] Utilizador não autenticado na sessão do WebSocket.");
                return;
            }

            // Extrai o UUID do utilizador autenticado a partir do Principal
            UUID currentUserId = UUID.fromString(principal.getName());

            // 1. Guarda a mensagem no PostgreSQL
            MessageResponseDTO message = messageService.sendMessage(currentUserId, dto);
            log.info("[WebSocket] Mensagem guardada no banco com ID: {}", message.id());

            // 2. Notifica todos os utilizadores inscritos na sala
            messagingTemplate.convertAndSend("/topic/rooms/" + dto.roomId(), message);
            log.info("[WebSocket] Mensagem transmitida para /topic/rooms/{}", dto.roomId());

        } catch (Exception e) {
            log.error("[WebSocket] Erro ao processar mensagem via WebSocket: ", e);
        }
    }
}