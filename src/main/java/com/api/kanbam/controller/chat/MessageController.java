package com.api.kanbam.controller.chat;

import java.net.URI;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.api.kanbam.domain.dtos.chat.MessageResponseDTO;
import com.api.kanbam.domain.dtos.chat.SendMessageDTO;
import com.api.kanbam.services.chat.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @AuthenticationPrincipal String userIdStr,
            @Valid @RequestBody SendMessageDTO dto) {

        log.info("📩 [MessageController] Recebida requisicao POST /messages do usuario: {} com DTO: {}", userIdStr, dto);

        try {
            UUID currentUserId = UUID.fromString(userIdStr);
            MessageResponseDTO message = messageService.sendMessage(currentUserId, dto);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(message.id())
                    .toUri();

            log.info("✅ [MessageController] Mensagem criada com sucesso! ID: {}", message.id());
            return ResponseEntity.created(location).body(message);

        } catch (Exception e) {
            log.error("💥 [MessageController] Erro ao enviar mensagem via REST: ", e);
            throw e;
        }
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageResponseDTO>> getRoomMessagesHistory(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable UUID roomId,
            @PageableDefault(size = 30, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("🔍 [MessageController] Buscando historico GET /rooms/{}/messages para usuario: {}", roomId, userIdStr);

        try {
            UUID currentUserId = UUID.fromString(userIdStr);
            Page<MessageResponseDTO> history = messageService.getRoomMessagesHistory(roomId, currentUserId, pageable);

            log.info("✅ [MessageController] Historico retornado com sucesso! Total elementos: {}, Pagina atual: {}",
                    history.getTotalElements(), history.getNumber());

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("💥 [MessageController] Erro ao buscar historico de mensagens: ", e);
            throw e;
        }
    }
}