package com.api.kanbam.listeners.chat;

import com.api.kanbam.domain.entities.chat.ChatParticipant;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.kanbam.domain.dtos.chat.MessageSentEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagesEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSentEvent(MessageSentEventDTO event) {
        log.info("EVENTO RECEBIDO NO LISTENER! Participantes: {}", event.roomParticipants().size());
        UUID senderId = event.messageResponseDTO().senderId();

// Envia a mensagem individualmente apenas para os OUTROS membros da sala
        for (ChatParticipant participant : event.roomParticipants()) {
            if (!participant.getUserId().equals(senderId)) {
                messagingTemplate.convertAndSend(
                        "/topic/user/" + participant.getUserId() + "/messages",
                        event.messageResponseDTO()
                );
                log.info("SEND MESSAGE to user {}: {}", participant.getUserId(), event.messageResponseDTO());            }
        }
    }
}