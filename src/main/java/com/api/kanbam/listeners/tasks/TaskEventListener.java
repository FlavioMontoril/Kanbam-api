package com.api.kanbam.listeners.tasks;

import com.api.kanbam.domain.dtos.task.TaskChangeStatusDTO;
import com.api.kanbam.domain.dtos.task.TaskCreatedEventDTO;
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
        log.info("Evento WebSocket enviado com sucesso para os IDs arquivados. {}", event.archivedTasks());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreatedEvent(TaskCreatedEventDTO event) {
        messagingTemplate.convertAndSend("/topic/task-created", event.createdTask());
        log.info("[Evento WebSocket enviado]: Tarefa criada {}", event.createdTask());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChangedEvent(TaskChangeStatusDTO event) {
        messagingTemplate.convertAndSend("/topic/task-status-changed", event.changeStatus());
        log.info("Evento WebSocket enviado: Status alterado {}", event.changeStatus());
    }
}
