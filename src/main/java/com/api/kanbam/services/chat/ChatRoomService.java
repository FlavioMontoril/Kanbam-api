package com.api.kanbam.services.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.api.kanbam.domain.dtos.chat.RoomCreatedEventDTO;
import org.springframework.context.ApplicationEventPublisher;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoomResponseDTO createRoom(UUID currentUserId, CreateChatRoomDTO dto) {
        log.info("🚀 [ChatRoomService] Iniciando criacao de sala. Usuario Criador: {}, DTO Recebido: {}", currentUserId, dto);

        try {
            // 1. Tratamento para conversa direta (1-para-1)
            if (dto.type() == ChatType.DIRECT) {
                if (dto.targetUserIds() == null || dto.targetUserIds().isEmpty()) {
                    log.error("[ChatRoomService] targetUserIds esta nulo ou vazio para chat DIRECT.");
                    throw new IllegalArgumentException("Para chat direto, informe o ID do usuário de destino.");
                }

                // Extrai o ID do destinatário ignorando o ID do próprio criador
                UUID targetUserId = dto.targetUserIds().stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst()
                        .orElse(dto.targetUserIds().get(0));

                log.info("🔍 [ChatRoomService] Verificando se ja existe sala DIRETA entre currentUserId: {} e targetUserId: {}", currentUserId, targetUserId);

                // Busca se já existe uma sala direta criada entre estes dois usuários
                var existingRoom = chatRoomRepository.findDirectRoomBetweenUsers(
                        ChatType.DIRECT, currentUserId, targetUserId
                );

                if (existingRoom.isPresent()) {
                    log.info("[ChatRoomService] Sala direta ja existente encontrada (ID: {}). Retornando sala existente.", existingRoom.get().getId());
                    return ChatRoomResponseDTO.fromEntity(existingRoom.get());
                }
            }

            // 2. Criação da entidade sala
            log.info("[ChatRoomService] Criando nova entidade ChatRoom (Tipo: {}, Nome: {})...", dto.type(), dto.name());
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

            log.info("[ChatRoomService] Adicionado criador {} como ADMIN.", currentUserId);

            // 4. Adicionar os demais participantes informados
            if (dto.targetUserIds() != null) {
                for (UUID targetId : dto.targetUserIds()) {
                    if (!targetId.equals(currentUserId)) {
                        participants.add(ChatParticipant.builder()
                                .userId(targetId)
                                .role(RoleMember.MEMBER)
                                .room(room)
                                .build());
                        log.info("[ChatRoomService] Adicionado participante {} como MEMBER.", targetId);
                    }
                }
            }

            room.setParticipants(participants);
            ChatRoom savedRoom = chatRoomRepository.save(room);

            ChatRoomResponseDTO responseDTO = ChatRoomResponseDTO.fromEntity(savedRoom);

            // 2. Publica o evento de domínio no Spring
            // eventPublisher.publishEvent(new RoomCreatedEventDTO(currentUserId, savedRoom.getParticipants(), responseDTO));
            // Dispara o evento WebSocket APENAS se for um GRUPO
            if (savedRoom.getType() == ChatType.GROUP) {
                eventPublisher.publishEvent(
                        new RoomCreatedEventDTO(currentUserId, savedRoom.getParticipants(), responseDTO)
                );
            }

            log.info("[ChatRoomService] Sala salva com sucesso! ID da Sala: {}", savedRoom.getId());
            return responseDTO;

        } catch (Exception e) {
            log.error("[ChatRoomService] ERRO ao criar sala no banco: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getUserRooms(UUID userId) {
        log.info("[ChatRoomService] Buscando todas as salas do usuario: {}", userId);
        return chatRoomRepository.findAllByUserId(userId).stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponseDTO getRoomById(UUID roomId, UUID userId) {
        log.info("[ChatRoomService] Buscando sala por ID: {} para o usuario: {}", roomId, userId);
        validateUserInRoom(roomId, userId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        return ChatRoomResponseDTO.fromEntity(room);
    }

    public void validateUserInRoom(UUID roomId, UUID userId) {
        boolean isParticipant = chatParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
        if (!isParticipant) {
            log.warn("[ChatRoomService] Acesso negado: Usuario {} nao e participante da sala {}", userId, roomId);
            throw new SecurityException("Usuário não tem permissão para acessar esta sala.");
        }
    }

    @Transactional(readOnly = true)
    public List<UUID> getParticipantUserIds(UUID roomId) {
        log.info("[ChatRoomService] Buscando IDs dos participantes da sala: {}", roomId);
        return chatParticipantRepository.findUserIdsByRoomId(roomId);
    }
}