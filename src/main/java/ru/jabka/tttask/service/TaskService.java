package ru.jabka.tttask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.jabka.tttask.client.UserClient;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Status;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.model.TaskRequest;
import ru.jabka.tttask.model.UpdateTask;
import ru.jabka.tttask.model.UserResponse;
import ru.jabka.tttask.repository.TaskRepository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserClient userClient;

    @Transactional(rollbackFor = Throwable.class)
    public Task create(final TaskRequest taskRequest) {
        validateTaskRequest(taskRequest);
        return taskRepository.insert(Task.builder()
                .title(taskRequest.title())
                .description(taskRequest.description())
                .status(Status.TO_DO)
                .deadLine(taskRequest.deadLine())
                .author(taskRequest.author())
                .assignee(taskRequest.assignee())
                .build());
    }

    @Transactional(rollbackFor = Throwable.class)
    public Task update(final UpdateTask updateTask) {
        validateUpdateRequest(updateTask);
        Task task = getById(updateTask.id());
        Task.TaskBuilder taskBuilder = Task.builder().id(task.id());
        ofNullable(updateTask.title()).ifPresentOrElse(
                taskBuilder::title,
                () -> taskBuilder.title(task.title()));
        ofNullable(updateTask.description()).ifPresentOrElse(
                taskBuilder::description,
                () -> taskBuilder.description(task.description()));
        ofNullable(updateTask.deadLine()).ifPresentOrElse(
                taskBuilder::deadLine,
                () -> taskBuilder.deadLine(task.deadLine()));
        ofNullable(updateTask.assignee()).ifPresentOrElse(
                taskBuilder::assignee,
                () -> taskBuilder.assignee(task.assignee()));
        ofNullable(updateTask.status()).ifPresentOrElse(
                taskBuilder::status,
                () -> taskBuilder.status(task.status()));
        return taskRepository.update(taskBuilder.build());
    }

    @Transactional(readOnly = true)
    public Task getById(final Long id) {
        return taskRepository.getById(id);
    }

    @Transactional(readOnly = true)
    public Set<Task> getAllByFilter(final Status status, final Long assignee) {
        ofNullable(assignee).ifPresent(x -> checkMembersExists(Collections.singleton(x)));
        Long statusId = status == null ? null : status.getId();
        return Set.copyOf(taskRepository.getByFilter(statusId, assignee));
    }

    private void validateTaskRequest(final TaskRequest taskRequest) {
        ofNullable(taskRequest).orElseThrow(() -> new BadRequestException("Заполните данные задачи"));
        if (!StringUtils.hasText(taskRequest.title())) {
            throw new BadRequestException("Заполните заголовок задачи");
        }
        if (!StringUtils.hasText(taskRequest.description())) {
            throw new BadRequestException("Заполните описание задачи");
        }
        ofNullable(taskRequest.deadLine()).orElseThrow(() -> new BadRequestException("Заполните срок исполнения задачи"));
        ofNullable(taskRequest.author()).orElseThrow(() -> new BadRequestException("Заполните автора задачи"));
        ofNullable(taskRequest.assignee()).orElseThrow(() -> new BadRequestException("Заполните исполнителя задачи"));
        Set<Long> members = Set.of(taskRequest.author(), taskRequest.assignee());
        checkMembersExists(members);
    }

    private void validateUpdateRequest(final UpdateTask updateTask) {
        ofNullable(updateTask).orElseThrow(() -> new BadRequestException("Заполните данные для обновления"));
        ofNullable(updateTask.id()).orElseThrow(() -> new BadRequestException("Не указан id задачи, которую необходимо обновить"));
        ofNullable(updateTask.assignee()).ifPresent(x -> checkMembersExists(Collections.singleton(x)));
    }

    private void checkMembersExists(final Set<Long> members) {
        Set<Long> allByIds = userClient.getAllByIds(members).stream()
                .map(UserResponse::id)
                .collect(Collectors.toSet());
        for (Long member : members) {
            if (!allByIds.contains(member)) {
                throw new BadRequestException(String.format("Пользователь с id = %d не найден", member));
            }
        }
    }
}