package com.api.kanbam.domain.repositories;

import com.api.kanbam.domain.dtos.task.TaskMetricsDTO;
import com.api.kanbam.domain.dtos.task.TaskResponseDTO;
import com.api.kanbam.domain.entities.Task;
import com.api.kanbam.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<TaskResponseDTO> findByCode(String code);

//    Page<Task> findByStatusAndArchivedFalse(TaskStatus status, Pageable pageable);

    @Query("""
        SELECT t FROM Task t 
        WHERE t.archived = false 
          AND (:status IS NULL OR t.status = :status) 
          AND (
               :search IS NULL 
               OR :search = '' 
               OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) 
               OR LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (CAST(:startDate AS timestamp) IS NULL OR t.createdAt >= :startDate)
          AND (CAST(:endDate AS timestamp) IS NULL OR t.createdAt <= :endDate)
    """)
    Page<Task> findAllPagedAndFiltered(
            @Param("status") TaskStatus status,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    List<Task> findByArchivedFalse();

    long countByStatus(TaskStatus status);

//    @Query("SELECT t FROM Task t WHERE t.status = com.api.kanbam.domain.enums.TaskStatus.CANCELED AND t.archived = false")
//    List<Task> findCanceledAndNonArchivedTasks();

    // 🎯 Busca tarefas canceladas e não-arquivadas onde a última mudança para CANCELED ocorreu antes da data limite
    @Query("""
        SELECT DISTINCT t FROM Task t 
        JOIN t.histories h 
        WHERE t.status = TaskStatus.CANCELED 
          AND t.archived = false 
          AND h.currentStatus = TaskStatus.CANCELED 
          AND h.movedAt <= :cutoffDate
    """)
    List<Task> findCanceledTasksOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("""
        SELECT new com.api.kanbam.domain.dtos.task.TaskMetricsDTO(
            MONTH(t.createdAt),
            COUNT(t),
            SUM(CASE WHEN t.status = com.api.kanbam.domain.enums.TaskStatus.OPEN THEN 1L ELSE 0L END),
            SUM(CASE WHEN t.status = com.api.kanbam.domain.enums.TaskStatus.DONE THEN 1L ELSE 0L END),
            SUM(CASE WHEN t.status = com.api.kanbam.domain.enums.TaskStatus.CANCELED THEN 1L ELSE 0L END)
        )
        FROM Task t
        WHERE YEAR(t.createdAt) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(t.createdAt)
        ORDER BY MONTH(t.createdAt) ASC
    """)
    List<TaskMetricsDTO> getTaskMetricsForCurrentYear();
}

