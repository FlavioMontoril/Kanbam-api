package com.api.kanbam.controller;

import com.api.kanbam.domain.dtos.commons.Pagination;
import com.api.kanbam.domain.dtos.task.TaskRequestDTO;
import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.dtos.task.TasksCountDTO;
import com.api.kanbam.domain.dtos.task.UpdateTaskStatusDTO;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody TaskRequestDTO data){
        taskService.createTask(data);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created Task Succesfully");
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> findAllTasks(){
        List<TaskResponseDTO> tasks = taskService.findAllTasks();
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping("/count")
    public ResponseEntity<TasksCountDTO> countTasks(){
        return ResponseEntity.ok(taskService.countTaskPerStatus());
    }

    @GetMapping("/paged")
    public ResponseEntity<Pagination<TaskResponseDTO>> findTaskByStatus(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam() int page,
            @RequestParam(defaultValue = "10") int size
            ){
        Pagination<TaskResponseDTO> response = taskService.findAllTasksPerStatus(status, page, size);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping("/{taskId}/status")

    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskStatusDTO dto
            ){
        TaskResponseDTO response = taskService.moveTaskStatus(taskId, dto);

        if(response == null){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
