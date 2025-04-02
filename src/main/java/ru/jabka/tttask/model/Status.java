package ru.jabka.tttask.model;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum Status {
    TO_DO(1L),
    IN_PROGRESS(2L),
    DONE(3L),
    DELETED(4L);

    private final Long id;

    Status(Long id) {
        this.id = id;
    }

    public static Status byId(Long id) {
        return Stream.of(Status.values())
                .filter(x -> id.equals(x.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(String.format("Статус с id = %d не найден", id)));
    }
}