package com.api.kanbam.services;

import com.api.kanbam.domain.dtos.commons.Pagination;
import com.api.kanbam.domain.dtos.task.*;
import com.api.kanbam.domain.entities.Task;
//import com.api.kanbam.domain.entities.User;
import com.api.kanbam.domain.enums.TaskStatus;
import com.api.kanbam.domain.repositories.TaskRepository;
//import com.api.kanbam.domain.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    @org.springframework.beans.factory.annotation.Value("${auth.server.url:http://localhost:8081}")
    private String authServerUrl;

    private static final int MAX_SIZE = 20;
    private final TaskRepository taskRepository;
    private final TaskHistoryService taskHistoryService;
//    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RestClient restClient;
    private final HttpServletRequest currentRequest;

    @Transactional
    public void createTask(TaskRequestDTO data){

        taskRepository.findByCode(data.code()).ifPresent(hasTask -> {throw new RuntimeException("Existe tarefa cadastrada com este código");
        });

//        User assigneeUser = null;
        if (data.userId() != null ) {
//            assigneeUser = userRepository.findById(data.userId())
//                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));

            // 1. Extrai o cabeçalho "Authorization" da requisição do React
            String authHeader = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autenticação não fornecido.");
            }

            try {
                restClient.get()
                        .uri(authServerUrl + "/api/users/{id}", data.userId())
                        .header(HttpHeaders.AUTHORIZATION, authHeader) // <--- Repassa o token aqui
                        .retrieve()
                        .toBodilessEntity(); // Apenas verifica se retorna 200 OK
            } catch (HttpClientErrorException.NotFound e) {
                throw new RuntimeException("Usuário responsável (assignee) não encontrado!");
            }catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido ou expirado ao consultar serviço de usuários.");
            }
        }

            Task task = Task.builder()
                    .code(data.code())
                    .title(data.title())
                    .description(data.description())
                    .reporter(data.reporter())
                    .assignee(data.assignee())
                    .userId(data.userId())
//                    .user(assigneeUser)
                    .build();

        Task newTask = taskRepository.saveAndFlush(task);

        TaskResponseDTO createdTask = new TaskResponseDTO(newTask);

        eventPublisher.publishEvent(new TaskCreatedEventDTO(createdTask));
        log.info("{} Tarefa criada com sucesso.", createdTask);
    }

    public List<TaskResponseDTO> findAllTasks(){
      return taskRepository.findByArchivedFalse().stream().map(TaskResponseDTO::new).toList();
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

        Task updatedStatus = taskRepository.save(task);

        taskHistoryService.recordStatusChange(updatedStatus, previousStatus, newStatus);


        TaskResponseDTO changeStatus = new TaskResponseDTO(updatedStatus);
        eventPublisher.publishEvent(new TaskChangeStatusDTO(changeStatus));
        log.info("{} Atualizado status com sucesso.", changeStatus);


        return  changeStatus;
    }

    public Pagination<TaskResponseDTO> findAllTasksPaged(TaskStatus status, String search, LocalDateTime startDate,LocalDateTime endDate, int page, int size){

        int validPage = Math.max(page, 0);
        int validSize = Math.clamp(size, 1, MAX_SIZE);

        Pageable pageable = PageRequest.of(validPage, validSize);

        // Ajusta o início do dia (00:00:00)
        LocalDateTime startDateTime = (startDate != null) ? startDate.toLocalDate().atStartOfDay() : null;

        // Ajusta o fim do dia (23:59:59.999999999)
        LocalDateTime endDateTime = (endDate != null) ? endDate.toLocalDate().atTime(LocalTime.MAX) : null;

        // Tratamento básico para evitar buscas desnecessárias se a string for vazia
        String querySearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<TaskResponseDTO> task;

        task = taskRepository.findAllPagedAndFiltered(status, querySearch, startDateTime, endDateTime, pageable).map(TaskResponseDTO::new);

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

    @Transactional
//    @Scheduled(fixedRate = 259200000L) //Executa a cada 3 dias (3 dias * 24h * 60m * 60s * 1000ms = 259200000 ms)
    @Scheduled(fixedRate = 60000) //Executa com 1 minuto
    public void archiveOldCanceledTasks(){
//        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);
        LocalDateTime limitDate = LocalDateTime.now().minusMinutes(1);

        List<Task> tasksToArchive = taskRepository.findCanceledTasksOlderThan(limitDate);

        if(tasksToArchive.isEmpty()){
            log.info("Nenhuma tarefa cancelada para arquivar.");
            return;
        }

        tasksToArchive.forEach(task -> task.setArchived(true));
        taskRepository.saveAll(tasksToArchive);

        //Coleta a Task arquivada
        List<TaskResponseDTO> archivedTasks = tasksToArchive.stream().map(TaskResponseDTO::new).toList();

        //Dispara o evento WebSocket contendo a lista dos IDs arquivados
        eventPublisher.publishEvent(new TasksArchivedEventDTO(archivedTasks));
        log.info("{} tarefas canceladas foram arquivadas com sucesso.", tasksToArchive.size());
    }

    @Transactional(readOnly = true)
    public List<TaskMetricsDTO> getCurrentYearTaskMetrics() {
        return taskRepository.getTaskMetricsForCurrentYear();
    }
}