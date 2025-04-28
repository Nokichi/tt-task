package ru.jabka.tttask.model;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String username,
        UserRole role
) {
}