package ru.jabka.tttask.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Report;
import ru.jabka.tttask.repository.mapper.ReportMapper;

import java.time.LocalDate;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ReportMapper reportMapper;

    private static final String GET_REPORT_BY_ASSIGNEE_GROUP = """
            WITH date_filtered_tasks AS (
                SELECT
                    id,
                    assignee,
                    status,
                    created_at,
                    updated_at
                FROM tt.task
                WHERE
                    assignee IN (:assignee_ids)
                    AND updated_at BETWEEN :start_date AND :end_date
            ),
            task_stats AS (
                SELECT
                    assignee,
                    COUNT(*) AS total_tasks,
                    COUNT(CASE WHEN status = 1 THEN 1 END) AS todo_count,
                    COUNT(CASE WHEN status = 2 THEN 1 END) AS in_progress_count,
                    COUNT(CASE WHEN status = 3 THEN 1 END) AS done_count,
                    AVG(
                        CASE
                            WHEN status = 3
                            THEN EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600
                        END
                    ) AS avg_hours_to_done
                FROM date_filtered_tasks
                GROUP BY assignee
            ),
            top_assignees AS (
                SELECT
                    assignee,
                    total_tasks
                FROM task_stats
                ORDER BY total_tasks DESC
                LIMIT 3
            ),
            aggregated_stats AS (
                SELECT
                    COUNT(*) AS total_team_tasks,
                    COUNT(CASE WHEN status = 1 THEN 1 END) AS team_todo_count,
                    COUNT(CASE WHEN status = 2 THEN 1 END) AS team_in_progress_count,
                    COUNT(CASE WHEN status = 3 THEN 1 END) AS team_done_count,
                    ROUND(
                        AVG(
                            CASE
                                WHEN status = 3
                                THEN EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600
                            END
                        )::numeric, 5
                    ) AS team_avg_hours_to_done
                FROM date_filtered_tasks
            )
            SELECT
                a.total_team_tasks,
                a.team_todo_count,
                a.team_in_progress_count,
                a.team_done_count,
                a.team_avg_hours_to_done,
                (
                    SELECT json_agg(
                        json_build_object(
                            'assigneeId', assignee,
                            'tasksCount', total_tasks
                        )
                    )
                    FROM top_assignees
                ) AS top_assignees_json
            FROM aggregated_stats a
            """;

    public Report getByTeam(final Set<Long> assigneeIds, final LocalDate startDate, final LocalDate endDate) {
        try {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("assignee_ids", assigneeIds)
                    .addValue("start_date", startDate)
                    .addValue("end_date", endDate);
            return jdbcTemplate.queryForObject(GET_REPORT_BY_ASSIGNEE_GROUP, parameterSource, reportMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BadRequestException(String.format("Данные по группе пользователей assigneeIds = {%s} не найдены", assigneeIds));
        }
    }
}