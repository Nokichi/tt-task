package ru.jabka.tttask.repository.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.jabka.tttask.model.MemberWithTaskCount;
import ru.jabka.tttask.model.Report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ReportMapper implements RowMapper<Report> {

    @Override
    public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
        String topAssigneesJson = rs.getString("top_assignees_json");
        ObjectMapper mapper = new ObjectMapper();
        MemberWithTaskCount[] memberWithTaskCounts;
        try {
            memberWithTaskCounts = mapper.readValue(topAssigneesJson, MemberWithTaskCount[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при диссериализации объекта top_assignees_json");
        }
        return Report.builder()
                .totalTeamTasks(rs.getLong("total_team_tasks"))
                .todoTeamTasks(rs.getLong("team_todo_count"))
                .inProgressTeamTasks(rs.getLong("team_in_progress_count"))
                .doneTeamTasks(rs.getLong("team_done_count"))
                .avgHoursTaskToDone(rs.getDouble("team_avg_hours_to_done"))
                .topMembersWithTasksCount(List.of(memberWithTaskCounts))
                .build();
    }
}