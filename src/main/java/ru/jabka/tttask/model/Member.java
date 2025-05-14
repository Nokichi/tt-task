package ru.jabka.tttask.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Member(
        Long teamId,
        Long memberId,
        LocalDateTime modifiedAt
) {
}