package ru.jabka.tttask.model.history;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TaskHistory implements Serializable {

    private Long taskId;
    private TaskData data;
    private Long createdBy;
    private LocalDateTime moment;
}