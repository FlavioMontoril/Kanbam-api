package com.api.kanbam.services;

import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.entities.TaskHistory;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.domain.repositories.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

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
}
