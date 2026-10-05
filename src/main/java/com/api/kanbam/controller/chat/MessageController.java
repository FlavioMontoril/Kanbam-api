package com.api.kanbam.controller.chat;

import java.net.URI;
import java.util.UUID;

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

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @AuthenticationPrincipal String userIdStr,
            @Valid @RequestBody SendMessageDTO dto) {
        Long currentUserId = Long.parseLong(userIdStr);
        MessageResponseDTO message = messageService.sendMessage(currentUserId, dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(message.id())
                .toUri();

        return ResponseEntity.created(location).body(message);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageResponseDTO>> getRoomMessagesHistory(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable UUID roomId,
            @PageableDefault(size = 30, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = Long.parseLong(userIdStr);
        Page<MessageResponseDTO> history = messageService.getRoomMessagesHistory(roomId, currentUserId, pageable);
        return ResponseEntity.ok(history);
    }
}