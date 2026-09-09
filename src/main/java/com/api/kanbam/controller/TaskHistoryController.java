package com.api.kanbam.controller;

import com.api.kanbam.domain.dtos.taskHistory.TaskHistoryResponseDTO;
import com.api.kanbam.services.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tasks-histories")
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    @GetMapping("/{taskId}")
    public ResponseEntity<List<TaskHistoryResponseDTO>> findAllTaskHistories(@PathVariable UUID taskId){
        List<TaskHistoryResponseDTO> histories = taskHistoryService.findAllHistoriesByTaskId(taskId);

        return ResponseEntity.status(HttpStatus.OK).body(histories);
    }
}
