package ru.jabka.tttask.model;

import lombok.Builder;

@Builder
public record ServiceResponse(Boolean success, String message) {
}