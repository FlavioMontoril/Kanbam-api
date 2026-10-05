package com.api.kanbam.domain.repositories.chat;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.kanbam.domain.entities.chat.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Busca o histórico de mensagens de uma sala com suporte a Paginação (Spring Data Pageable)
    // O Pageable permite ordenar por timestamp DESC e limitar a quantidade (ex: últimas 30 mensagens)
    Page<Message> findByRoomIdOrderByTimestampDesc(UUID roomId, Pageable pageable);
}