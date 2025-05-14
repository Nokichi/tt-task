package ru.jabka.tttask.model;

import lombok.Builder;

@Builder
public record MemberWithTaskCount(
        Long assigneeId,
        Long tasksCount
) {
}