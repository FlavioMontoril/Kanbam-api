package com.api.kanbam.listeners.tasks;

import com.api.kanbam.domain.dtos.task.TaskChangeStatusDTO;
import com.api.kanbam.domain.dtos.task.TaskCreatedEventDTO;
import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.dtos.task.TasksArchivedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTasksArchivedEvent(TasksArchivedEventDTO event) {

        List<TaskResponseDTO> tasks = event.archivedTasks();

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        Set<UUID> recipients = new HashSet<>();

        // 1. Acumula os IDs de TODOS os participantes de TODAS as tarefas arquivadas
        for (TaskResponseDTO task : tasks) {
            if (task.userId() != null) {
                recipients.add(task.userId());
            }

            if (task.reporterId() != null) {
                recipients.add(task.reporterId());
            }
        }

        // 2. Notifica cada destinatário acumulado uma única vez
        for (UUID recipientId : recipients) {
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/tasks-archived",
                    event);
        }

        log.info("Evento WebSocket enviado com sucesso para os IDs arquivados. {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreatedEvent(TaskCreatedEventDTO event) {
        messagingTemplate.convertAndSendToUser( event.createdTask().userId().toString(), "/queue/task-created", event);
        log.info("[Evento WebSocket enviado]: Tarefa criada {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChangedEvent(TaskChangeStatusDTO event) {
        messagingTemplate.convertAndSend("/topic/task-status-changed", event);
        log.info("Evento WebSocket enviado: Status alterado {}", event);
    }
}
