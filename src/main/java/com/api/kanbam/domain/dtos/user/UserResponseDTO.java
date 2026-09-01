package com.api.kanbam.domain.dtos.user;

import com.api.kanbam.domain.entities.User;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email
) {

    public UserResponseDTO(User user){
        this(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
