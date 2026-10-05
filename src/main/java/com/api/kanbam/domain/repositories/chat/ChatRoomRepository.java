package com.api.kanbam.domain.repositories.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.kanbam.domain.entities.chat.ChatRoom;
import com.api.kanbam.domain.enums.ChatType;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    // Buscar todas as salas em que um determinado usuário participa
    @Query("SELECT DISTINCT r FROM ChatRoom r JOIN r.participants p WHERE p.userId = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    // Verificar/Buscar se já existe uma conversa DIRETA (1-para-1) entre dois usuários específicos
    // Útil para não criar salas duplicadas ao clicar em "Iniciar Conversa"
    @Query("""
        SELECT r FROM ChatRoom r 
        JOIN r.participants p1 
        JOIN r.participants p2 
        WHERE r.type = :type 
          AND p1.userId = :user1Id 
          AND p2.userId = :user2Id
    """)
    Optional<ChatRoom> findDirectRoomBetweenUsers(
        @Param("type") ChatType type,
        @Param("user1Id") Long user1Id,
        @Param("user2Id") Long user2Id
    );
}