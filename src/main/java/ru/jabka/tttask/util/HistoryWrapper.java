package ru.jabka.tttask.util;

import lombok.experimental.UtilityClass;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.model.history.TaskData;
import ru.jabka.tttask.model.history.TaskHistory;

@UtilityClass
public class HistoryWrapper {

    public TaskHistory prepareMessage(Task task, Long createdBy) {
        TaskData taskData = TaskData.builder()
                .title(task.title())
                .description(task.description())
                .status(task.status())
                .assignee(task.assignee())
                .deadLine(task.deadLine())
                .build();
        return new TaskHistory()
                .setTaskId(task.id())
                .setData(taskData)
                .setCreatedBy(createdBy)
                .setMoment(task.updatedAt());
    }
}