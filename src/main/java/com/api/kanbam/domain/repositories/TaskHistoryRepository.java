package com.api.kanbam.domain.repositories;

import com.api.kanbam.domain.entities.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {

    List<TaskHistory> findAllByTaskId(UUID taskId);
}
