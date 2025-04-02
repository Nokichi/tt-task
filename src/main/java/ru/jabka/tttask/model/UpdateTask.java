package ru.jabka.tttask.model;

import java.time.LocalDate;

public record UpdateTask(
        Long id,
        String title,
        String description,
        LocalDate deadLine,
        Long assignee,
        Status status,
        Long editor //TODO пока для справочной информации
) {
}