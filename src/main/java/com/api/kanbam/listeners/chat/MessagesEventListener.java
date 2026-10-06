package com.api.kanbam.listeners.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.kanbam.domain.dtos.chat.MessageSentEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagesEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSentEvent(MessageSentEventDTO event) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + event.roomId(),
                event.messageResponseDTO());
    }

}
