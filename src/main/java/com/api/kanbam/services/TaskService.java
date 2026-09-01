package com.api.kanbam.services;

import com.api.kanbam.domain.dtos.task.TaskRequestDTO;
import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.dtos.task.UpdateTaskStatusDTO;
import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.domain.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryService taskHistoryService;

    public void createTask(TaskRequestDTO data){
        taskRepository.findByCode(data.code()).ifPresent(hasTask -> {throw new RuntimeException("Existe tarefa cadastrada com este código");
        });
            Task task = Task.builder()
                    .code(data.code())
                    .title(data.title())
                    .description(data.description())
                    .reporter(data.reporter())
                    .assignee(data.assignee())
                    .build();

        taskRepository.save(task);
    }

    public List<TaskResponseDTO> findAllTasks(){
      return taskRepository.findAll().stream().map(TaskResponseDTO::new).toList();
    }

    @Transactional
    public TaskResponseDTO moveTaskStatus(UUID taskId, UpdateTaskStatusDTO dto){
        Task task = taskRepository.findById(taskId).orElseThrow(()-> new RuntimeException("Task Not Found"));

        TaskStatus previousStatus = task.getStatus();
        TaskStatus newStatus = dto.status();

        if (newStatus == task.getStatus()){
            return null;
        }

        if (task.getStatus() == TaskStatus.CANCELED){
            return null;
        }

        task.setStatus(newStatus);

        Task updatedTask = taskRepository.save(task);

        taskHistoryService.recordStatusChange(updatedTask, previousStatus, newStatus);

        return new TaskResponseDTO(updatedTask);
    }
}