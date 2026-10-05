package com.api.kanbam.domain.repositories.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.kanbam.domain.entities.chat.ChatParticipant;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    // Lista todos os participantes de uma sala específica
    List<ChatParticipant> findByRoomId(UUID roomId);

    // Verifica se um usuário faz parte de uma sala específica (ideal para validação de acesso)
    boolean existsByRoomIdAndUserId(UUID roomId, Long userId);

    // Busca o vínculo de um participante específico na sala
    Optional<ChatParticipant> findByRoomIdAndUserId(UUID roomId, Long userId);
}