package com.api.kanbam.services;

import com.api.kanbam.domain.dtos.commons.Pagination;
import com.api.kanbam.domain.dtos.task.TaskRequestDTO;
import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.dtos.task.TasksCountDTO;
import com.api.kanbam.domain.dtos.task.UpdateTaskStatusDTO;
import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.entities.User;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.domain.repositories.TaskRepository;
import com.api.kanbam.domain.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final int MAX_SIZE = 20;
    private final TaskRepository taskRepository;
    private final TaskHistoryService taskHistoryService;
    private final UserRepository userRepository;

    public void createTask(TaskRequestDTO data){

        taskRepository.findByCode(data.code()).ifPresent(hasTask -> {throw new RuntimeException("Existe tarefa cadastrada com este código");
        });

        User assigneeUser = null;
        if (data.userId() != null ) {
            assigneeUser = userRepository.findById(data.userId())
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));
        }

            Task task = Task.builder()
                    .code(data.code())
                    .title(data.title())
                    .description(data.description())
                    .reporter(data.reporter())
                    .assignee(data.assignee())
                    .user(assigneeUser)
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

    public Pagination<TaskResponseDTO> findAllTasksPerStatus(TaskStatus status, int page, int size){

        int validPage = Math.max(page, 0);
        int validSize = Math.clamp(size, 1, MAX_SIZE);

        Pageable pageable = PageRequest.of(validPage, validSize);
        Page<TaskResponseDTO> task;

        if(status != null){
            task = taskRepository.findByStatus(status, pageable).map(TaskResponseDTO::new);
        }else{
            task = taskRepository.findAll(pageable).map(TaskResponseDTO::new);
        }

        return new Pagination<>(
                task.getContent(),
                task.getNumber(),
                task.getTotalPages(),
                task.getTotalElements()
        );
    }

    public TasksCountDTO countTaskPerStatus(){
        long open = taskRepository.countByStatus(TaskStatus.OPEN);
        long done = taskRepository.countByStatus(TaskStatus.DONE);
        long cancelled = taskRepository.countByStatus(TaskStatus.CANCELED);
        long in_progress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long under_review = taskRepository.countByStatus(TaskStatus.UNDER_REVIEW);
        long total = open + done + cancelled + in_progress + under_review;

        return new TasksCountDTO(open, done, cancelled, in_progress, under_review, total);
    }
}