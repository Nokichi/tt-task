package ru.jabka.tttask.model;

public enum StatusTransition {

    TODO_TO_IN_PROGRESS(Status.TO_DO, Status.IN_PROGRESS),
    TODO_TO_DELETED(Status.TO_DO, Status.DELETED),
    IN_PROGRESS_TO_DONE(Status.IN_PROGRESS, Status.DONE),
    IN_PROGRESS_TO_DELETED(Status.IN_PROGRESS, Status.DELETED),
    DONE_TO_DELETED(Status.DONE, Status.DELETED);

    private final Status from;
    private final Status to;

    StatusTransition(Status from, Status to) {
        this.from = from;
        this.to = to;
    }

    public static StatusTransition findTransition(Status from, Status to) {
        for (StatusTransition transition : values()) {
            if (transition.from.equals(from) && transition.to.equals(to)) {
                return transition;
            }
        }
        return null;
    }
}