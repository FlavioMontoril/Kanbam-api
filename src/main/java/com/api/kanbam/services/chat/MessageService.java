package com.api.kanbam.services.chat;

import java.util.UUID;

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

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    @Transactional
    public MessageResponseDTO sendMessage(Long senderId, SendMessageDTO dto) {
        // 1. Valida se o usuário remetente realmente faz parte da sala
        chatRoomService.validateUserInRoom(dto.roomId(), senderId);

        ChatRoom room = chatRoomRepository.findById(dto.roomId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        // 2. Monta e salva a mensagem
        Message message = Message.builder()
                .senderId(senderId)
                .content(dto.content())
                .room(room)
                .build();

        Message savedMessage = messageRepository.save(message);

        return MessageResponseDTO.fromEntity(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getRoomMessagesHistory(UUID roomId, Long userId, Pageable pageable) {
        // Valida acesso antes de retornar o histórico
        chatRoomService.validateUserInRoom(roomId, userId);

        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable)
                .map(MessageResponseDTO::fromEntity);
    }
}