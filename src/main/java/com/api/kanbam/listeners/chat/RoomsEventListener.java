package com.api.kanbam.listeners.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.kanbam.domain.dtos.chat.RoomCreatedEventDTO;
import com.api.kanbam.domain.entities.chat.ChatParticipant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomsEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomsCreatedEvent(RoomCreatedEventDTO event) {

        UUID creatorId = event.creatorId();

        for (ChatParticipant participant : event.participants()) {
            if(!participant.getUserId().equals(creatorId)){
            messagingTemplate.convertAndSend("/topic/user/" + participant.getUserId() + "/rooms",
                    event.roomResponseDTO());
            }
        }
    }
}
