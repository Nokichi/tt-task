package ru.jabka.tttask.model;

import lombok.Builder;

import java.util.List;

@Builder
public record Report(
        Long totalTeamTasks,
        Long todoTeamTasks,
        Long inProgressTeamTasks,
        Long doneTeamTasks,
        Double avgHoursTaskToDone,
        List<MemberWithTaskCount> topMembersWithTasksCount
) {
}