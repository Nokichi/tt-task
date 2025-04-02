package ru.jabka.tttask.model;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record Task(
        Long id,
        String title,
        String description,
        Status status,
        LocalDate deadLine,
        Long assignee,
        Long author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}