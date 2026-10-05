package com.api.kanbam.controller.chat;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.api.kanbam.domain.dtos.chat.ChatRoomResponseDTO;
import com.api.kanbam.domain.dtos.chat.CreateChatRoomDTO;
import com.api.kanbam.services.chat.ChatRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chats/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponseDTO> createRoom(
            @AuthenticationPrincipal String userIdStr,
            @Valid @RequestBody CreateChatRoomDTO dto) {
        Long currentUserId = Long.parseLong(userIdStr);
        ChatRoomResponseDTO room = chatRoomService.createRoom(currentUserId, dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(room.id())
                .toUri();

        return ResponseEntity.created(location).body(room);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponseDTO>> getUserRooms(
            @AuthenticationPrincipal String userIdStr) {
        Long currentUserId = Long.parseLong(userIdStr);
        List<ChatRoomResponseDTO> rooms = chatRoomService.getUserRooms(currentUserId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getRoomById(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable UUID roomId) {
        Long currentUserId = Long.parseLong(userIdStr);
        ChatRoomResponseDTO room = chatRoomService.getRoomById(roomId, currentUserId);
        return ResponseEntity.ok(room);
    }
}