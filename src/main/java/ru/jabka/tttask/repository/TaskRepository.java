package ru.jabka.tttask.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.repository.mapper.TaskMapper;

import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TaskRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TaskMapper taskMapper;

    private static final String INSERT = """
            INSERT INTO tt.task (title, description, status, dead_line, author, assignee)
            VALUES (:title, :description, :status, :dead_line, :author, :assignee)
            RETURNING *
            """;

    private static final String GET_BY_ID = """
            SELECT t.*
            FROM tt.task t
            JOIN tt.status s ON s.id = t.status
            WHERE t.id = :id
              AND s.name <> 'DELETED'
            """;

    private static final String EXISTS_ACTIVE_TASKS_BY_ASSIGNEE = """
            SELECT EXISTS (
                SELECT 1
                FROM tt.task t
                JOIN tt.status s ON s.id = t.status
                WHERE t.assignee = :id
                  AND s.name NOT IN ('DELETED', 'DONE')
            ) AS has_active_tasks
            """;

    private static final String UPDATE = """
            UPDATE tt.task
            SET title = :title, description = :description, dead_line = :dead_line, assignee = :assignee, status = :status, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            RETURNING *
            """;

    private static final String GET_BY_FILTER = """
            SELECT *
            FROM tt.task t
            JOIN tt.status s ON s.id = t.status
            WHERE s.name <> 'DELETED'
              AND (
                  (:status IS NULL OR t.status = :status)
                  AND
                  (:assignee IS NULL OR t.assignee = :assignee)
              )
            """;

    public Task insert(final Task task) {
        return jdbcTemplate.queryForObject(INSERT, taskToSql(task), taskMapper);
    }

    public Task getById(final Long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, new MapSqlParameterSource("id", id), taskMapper);
        } catch (Throwable e) {
            throw new BadRequestException(String.format("Задача с id %d не найдена", id));
        }
    }

    public boolean existsActiveTasksByAssignee(final Long assigneeId) {
        return jdbcTemplate.queryForObject(EXISTS_ACTIVE_TASKS_BY_ASSIGNEE, new MapSqlParameterSource("id", assigneeId), Boolean.class);
    }

    public Task update(final Task task) {
        return jdbcTemplate.queryForObject(UPDATE, taskToSql(task), taskMapper);
    }

    public List<Task> getByFilter(final Long status, final Long assignee) {
        return jdbcTemplate.query(GET_BY_FILTER, new MapSqlParameterSource()
                .addValue("status", status, Types.INTEGER)
                .addValue("assignee", assignee, Types.INTEGER), taskMapper);
    }

    private MapSqlParameterSource taskToSql(Task task) {
        return new MapSqlParameterSource()
                .addValue("id", task.id())
                .addValue("title", task.title())
                .addValue("description", task.description())
                .addValue("dead_line", task.deadLine())
                .addValue("assignee", task.assignee())
                .addValue("status", task.status().getId())
                .addValue("author", task.author())
                .addValue("created_at", task.createdAt())
                .addValue("updated_at", task.updatedAt());
    }
}