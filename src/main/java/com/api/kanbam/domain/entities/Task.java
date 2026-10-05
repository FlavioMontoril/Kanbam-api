package com.api.kanbam.domain.entities;

import com.api.kanbam.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Task {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Setter(AccessLevel.NONE)
    @Column(name = "code", length = 10, updatable = false, nullable = false, unique = true)
    private String code;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reporter_id", nullable = false, updatable = false, length = 36)
    private UUID reporterId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private boolean archived = false;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

    // Substituição da relação @ManyToOne pelo UUID simples
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", length = 36)
    private UUID userId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskHistory> histories = new ArrayList<>();
}
