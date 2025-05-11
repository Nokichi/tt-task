package ru.jabka.tttask.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReportRequest(
        Long teamId,
        LocalDate startDate,
        LocalDate endDate
) {
}