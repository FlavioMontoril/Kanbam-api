package com.api.kanbam.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "users_local")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLocal {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", nullable = false, updatable = false, length = 36)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false, length = 10)
    private String role; // Recebe a role vinda do evento
}
