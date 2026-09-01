package com.api.kanbam.domain.repositories;

import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    public Optional<TaskResponseDTO> findByCode(String code);
}
