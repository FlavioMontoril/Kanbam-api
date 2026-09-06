package com.api.kanbam.domain.repositories;

import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<TaskResponseDTO> findByCode(String code);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    long countByStatus(TaskStatus status);

}
