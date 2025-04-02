package ru.jabka.tttask.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.jabka.tttask.model.Status;
import ru.jabka.tttask.model.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

@Component
public class TaskMapper implements RowMapper<Task> {

    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Task.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .status(Status.byId(rs.getLong("status")))
                .deadLine(rs.getObject("dead_line", LocalDate.class))
                .assignee(rs.getLong("assignee"))
                .author(rs.getLong("author"))
                .createdAt(rs.getObject("created_at", Timestamp.class).toLocalDateTime())
                .updatedAt(rs.getObject("updated_at", Timestamp.class).toLocalDateTime())
                .build();
    }
}