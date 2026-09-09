package com.api.kanbam.services;

import com.api.kanbam.domain.dtos.taskHistory.TaskHistoryResponseDTO;
import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.entities.TaskHistory;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.domain.repositories.TaskHistoryRepository;
import com.api.kanbam.domain.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskRepository taskRepository;

    public void recordStatusChange(Task task, TaskStatus previousStatus, TaskStatus newStatus){
        if(previousStatus == newStatus){
            return;
        }

        TaskHistory history = TaskHistory.builder()
                .previousStatus(previousStatus)
                .currentStatus(newStatus)
                .task(task)
                .build();

        taskHistoryRepository.save(history);

    }

    public List<TaskHistoryResponseDTO> findAllHistoriesByTaskId(UUID taskId){
        taskRepository.findById(taskId).orElseThrow(()-> {throw new RuntimeException("Tarefa não cadastrada ou id incorreto");
        });

        return taskHistoryRepository.findAllByTaskId(taskId).stream().map(TaskHistoryResponseDTO::new).toList();


    }
}
