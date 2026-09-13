package com.api.kanbam.listeners.tasks;

import com.api.kanbam.domain.dtos.task.TasksArchivedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTasksArchivedEvent(TasksArchivedEventDTO event) {
        messagingTemplate.convertAndSend("/topic/tasks-archived", event.archivedTasks());
        log.info("Evento WebSocket enviado com sucesso para os IDs arquivados. {}", event.archivedTasks().toString());
    }

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleTaskCreatedEvent(TaskCreatedEventDTO event) {
//        messagingTemplate.convertAndSend("/topic/tasks-created", event);
//        log.info("Evento WebSocket enviado: Tarefa criada {}", event.taskId());
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleTaskStatusChangedEvent(TaskStatusChangedEventDTO event) {
//        messagingTemplate.convertAndSend("/topic/tasks-status-changed", event);
//        log.info("Evento WebSocket enviado: Status alterado {}", event.taskId());
//    }
}
