package ru.jabka.tttask.model.history;

import lombok.Builder;
import ru.jabka.tttask.model.Status;

import java.time.LocalDate;

@Builder
public record TaskData(
        String title,
        String description,
        Status status,
        LocalDate deadLine,
        Long assignee
) {
}