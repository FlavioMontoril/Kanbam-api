package com.api.kanbam.services.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.kanbam.domain.dtos.chat.ChatRoomResponseDTO;
import com.api.kanbam.domain.dtos.chat.CreateChatRoomDTO;
import com.api.kanbam.domain.entities.chat.ChatParticipant;
import com.api.kanbam.domain.entities.chat.ChatRoom;
import com.api.kanbam.domain.enums.ChatType;
import com.api.kanbam.domain.enums.RoleMember;
import com.api.kanbam.domain.repositories.chat.ChatParticipantRepository;
import com.api.kanbam.domain.repositories.chat.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    public ChatRoomResponseDTO createRoom(Long currentUserId, CreateChatRoomDTO dto) {
        // 1. Tratamento para conversa direta (1-para-1)
        if (dto.type() == ChatType.DIRECT) {
            if (dto.targetUserIds() == null || dto.targetUserIds().isEmpty()) {
                throw new IllegalArgumentException("Para chat direto, informe o ID do usuário de destino.");
            }
            Long targetUserId = dto.targetUserIds().get(0);

            // Verifica se já existe uma sala direta criada entre estes dois usuários
            var existingRoom = chatRoomRepository.findDirectRoomBetweenUsers(
                ChatType.DIRECT, currentUserId, targetUserId
            );
            if (existingRoom.isPresent()) {
                return ChatRoomResponseDTO.fromEntity(existingRoom.get());
            }
        }

        // 2. Criação da sala
        ChatRoom room = ChatRoom.builder()
                .name(dto.type() == ChatType.GROUP ? dto.name() : null)
                .type(dto.type())
                .build();

        // 3. Adicionar o criador da sala como ADMIN
        List<ChatParticipant> participants = new ArrayList<>();
        participants.add(ChatParticipant.builder()
                .userId(currentUserId)
                .role(RoleMember.ADMIN)
                .room(room)
                .build());

        // 4. Adicionar os demais participantes informados
        if (dto.targetUserIds() != null) {
            for (Long targetId : dto.targetUserIds()) {
                if (!targetId.equals(currentUserId)) {
                    participants.add(ChatParticipant.builder()
                            .userId(targetId)
                            .role(RoleMember.MEMBER)
                            .room(room)
                            .build());
                }
            }
        }

        room.setParticipants(participants);
        ChatRoom savedRoom = chatRoomRepository.save(room);

        return ChatRoomResponseDTO.fromEntity(savedRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getUserRooms(Long userId) {
        return chatRoomRepository.findAllByUserId(userId).stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponseDTO getRoomById(UUID roomId, Long userId) {
        validateUserInRoom(roomId, userId);
        
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
                
        return ChatRoomResponseDTO.fromEntity(room);
    }

    public void validateUserInRoom(UUID roomId, Long userId) {
        boolean isParticipant = chatParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
        if (!isParticipant) {
            throw new SecurityException("Usuário não tem permissão para acessar esta sala.");
        }
    }
}