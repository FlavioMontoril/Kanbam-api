package com.api.kanbam.domain.dtos.user;

public record   UserRequestDTO(
        String name,
        String email,
        String password
) {
}
