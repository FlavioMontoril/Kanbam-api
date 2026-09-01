package com.api.kanbam.domain.entities;

import com.api.kanbam.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_histories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskHistory {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20, nullable = false, updatable = false)
    private TaskStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", length = 20, nullable = false, updatable = false)
    private TaskStatus currentStatus;

//    @Column(name = "moved_by", nullable = false)
//    private String movedBy; // Nome ou ID do usuário que fez o movimento

    @CreationTimestamp
    @Column(name = "moved_at", nullable = false, updatable = false)
    private LocalDateTime movedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
