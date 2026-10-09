package com.api.kanbam.services.chat;

import java.util.UUID;

import com.api.kanbam.domain.dtos.chat.MessageSentEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.kanbam.domain.dtos.chat.MessageResponseDTO;
import com.api.kanbam.domain.dtos.chat.SendMessageDTO;
import com.api.kanbam.domain.entities.chat.ChatRoom;
import com.api.kanbam.domain.entities.chat.Message;
import com.api.kanbam.domain.repositories.chat.ChatRoomRepository;
import com.api.kanbam.domain.repositories.chat.MessageRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MessageResponseDTO sendMessage(UUID senderId, SendMessageDTO dto) {
        // 1. Valida se o usuário remetente realmente faz parte da sala
        chatRoomService.validateUserInRoom(dto.roomId(), senderId);

        ChatRoom room = chatRoomRepository.findByIdWithParticipants(dto.roomId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        log.info("ROOM: {}", room);

        // 2. Monta e salva a mensagem
        Message message = Message.builder()
                .senderId(senderId)
                .content(dto.content())
                .room(room)
                .build();

        Message savedMessage = messageRepository.save(message);

        MessageResponseDTO response = MessageResponseDTO.fromEntity(savedMessage);

        // Dispara o evento de mensagem enviada (aguarda o commit para transmitir via WebSocket)
        eventPublisher.publishEvent(new MessageSentEventDTO(dto.roomId(), room.getParticipants(), response));

        return response;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getRoomMessagesHistory(UUID roomId, UUID userId, Pageable pageable) {
        // Valida acesso antes de retornar o histórico
        chatRoomService.validateUserInRoom(roomId, userId);

        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable)
                .map(MessageResponseDTO::fromEntity);
    }
}