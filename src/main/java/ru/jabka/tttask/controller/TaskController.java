package ru.jabka.tttask.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.jabka.tttask.model.Status;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.model.TaskRequest;
import ru.jabka.tttask.model.UpdateTask;
import ru.jabka.tttask.service.TaskService;

import java.util.Set;

@RestController
@Tag(name = "Задачи")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Task create(@RequestBody final TaskRequest taskRequest) {
        return taskService.create(taskRequest);
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable final Long id) {
        return taskService.getById(id);
    }

    @PatchMapping
    public Task update(@RequestBody final UpdateTask updateTask) {
        return taskService.update(updateTask);
    }

    @GetMapping
    public Set<Task> getByFilter(@RequestParam(required = false) final Status status,
                                 @RequestParam(required = false) final Long assignee) {
        return taskService.getAllByFilter(status, assignee);
    }
}