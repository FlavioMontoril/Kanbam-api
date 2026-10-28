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

//    Chat Direto (1-para-1): O destinatário só veja quando houver pelo menos 1 mensagem (ou se ele for o criador).
//    Grupo (GROUP): Todos os membros vejam o grupo imediatamente assim que forem adicionados.
    @Query("""
    SELECT DISTINCT r FROM ChatRoom r 
    JOIN r.participants p 
    WHERE p.userId = :userId 
      AND (
        r.type = com.api.kanbam.domain.enums.ChatType.GROUP
        OR p.role = com.api.kanbam.domain.enums.RoleMember.ADMIN 
        OR EXISTS (SELECT m FROM Message m WHERE m.room = r)
      )
""")
    List<ChatRoom> findAllByUserId(@Param("userId") UUID userId);

    // Buscar todas as salas em que um determinado usuário participa
//    @Query("SELECT DISTINCT r FROM ChatRoom r JOIN r.participants p WHERE p.userId = :userId")
//    List<ChatRoom> findAllByUserId(@Param("userId") UUID userId);

    // Consulta otimizada para localizar salas 1-para-1 independentemente da ordem dos participantes
    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.type = :type
          AND EXISTS (SELECT p1 FROM ChatParticipant p1 WHERE p1.room = r AND p1.userId = :user1Id)
          AND EXISTS (SELECT p2 FROM ChatParticipant p2 WHERE p2.room = r AND p2.userId = :user2Id)
    """)
    Optional<ChatRoom> findDirectRoomBetweenUsers(
            @Param("type") ChatType type,
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id
    );

    // Carrega a sala e inicializa a lista de participantes na mesma consulta (evita LAZY)
    @Query("SELECT DISTINCT r FROM ChatRoom r LEFT JOIN FETCH r.participants WHERE r.id = :roomId")
    Optional<ChatRoom> findByIdWithParticipants(@Param("roomId") UUID roomId);
}