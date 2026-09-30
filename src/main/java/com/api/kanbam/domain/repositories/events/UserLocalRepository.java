package com.api.kanbam.domain.repositories.events;

import com.api.kanbam.domain.entities.UserLocal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserLocalRepository extends JpaRepository<UserLocal, UUID> {
}
