package ru.jabka.tttask.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TaskRequest(
        String title,
        String description,
        LocalDate deadLine,
        Long author,
        Long assignee
) {
}