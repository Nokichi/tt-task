package ru.jabka.tttask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.jabka.tttask.client.UserClient;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Status;
import ru.jabka.tttask.model.StatusTransition;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.model.TaskRequest;
import ru.jabka.tttask.model.UpdateTask;
import ru.jabka.tttask.model.UserResponse;
import ru.jabka.tttask.model.UserRole;
import ru.jabka.tttask.repository.TaskRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
        Task updates = applyUpdates(getById(updateTask.id()), updateTask);
        return taskRepository.update(updates);
    }

    @Transactional(readOnly = true)
    public Task getById(final Long id) {
        return taskRepository.getById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> getByAssigneeId(final Long id) {
        return taskRepository.getByAssigneeId(id);
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
        ofNullable(taskRequest.deadLine()).ifPresentOrElse(this::validateDeadLine, () -> {
            throw new BadRequestException("Заполните срок исполнения задачи");
        });
        ofNullable(taskRequest.author()).orElseThrow(() -> new BadRequestException("Заполните автора задачи"));
        ofNullable(taskRequest.assignee()).orElseThrow(() -> new BadRequestException("Заполните исполнителя задачи"));
        Set<Long> members = Set.of(taskRequest.author(), taskRequest.assignee());
        checkMembersExists(members);
    }

    private void validateUpdateRequest(final UpdateTask updateTask) {
        ofNullable(updateTask).orElseThrow(() -> new BadRequestException("Заполните данные для обновления"));
        ofNullable(updateTask.id()).orElseThrow(() -> new BadRequestException("Не указан id задачи, которую необходимо обновить"));
        Long editorId = updateTask.editor();
        ofNullable(editorId).orElseThrow(() -> new BadRequestException("Не указан id пользователя, который редактирует задачу"));
        UserResponse editor = userClient.getAllByIds(Set.of(editorId))
                .stream()
                .filter(x -> editorId.equals(x.id())).findFirst()
                .orElseThrow(() -> new BadRequestException(String.format("Пользователь с id = %d, выполняющий редактирование, не найден", editorId)));
        ofNullable(updateTask.assignee()).ifPresent(x -> {
            if (!UserRole.MANAGER.equals(editor.role())) {
                throw new BadRequestException(String.format("Роль пользователя id = %d, выполняющего редактирование, не соответствует роли %s", editorId, UserRole.MANAGER));
            }
            checkMembersExists(Collections.singleton(x));
        });
        ofNullable(updateTask.deadLine()).ifPresent(this::validateDeadLine);
    }

    private void validateDeadLine(LocalDate deadLine) {
        if (deadLine.isBefore(LocalDate.now())) {
            throw new BadRequestException("Срок исполнения не может быть в прошлом");
        }
    }

    private Task applyUpdates(Task existedTask, final UpdateTask updateTask) {
        Task.TaskBuilder taskBuilder = Task.builder().id(existedTask.id());
        ofNullable(updateTask.title()).ifPresentOrElse(
                taskBuilder::title,
                () -> taskBuilder.title(existedTask.title()));
        ofNullable(updateTask.description()).ifPresentOrElse(
                taskBuilder::description,
                () -> taskBuilder.description(existedTask.description()));
        ofNullable(updateTask.deadLine()).ifPresentOrElse(
                taskBuilder::deadLine,
                () -> taskBuilder.deadLine(existedTask.deadLine()));
        ofNullable(updateTask.assignee()).ifPresentOrElse(
                taskBuilder::assignee,
                () -> taskBuilder.assignee(existedTask.assignee()));
        ofNullable(updateTask.status()).ifPresentOrElse(newStatus -> {
                    validateStatusTransition(existedTask.status(), newStatus);
                    taskBuilder.status(newStatus);
                },
                () -> taskBuilder.status(existedTask.status()));
        return taskBuilder.build();
    }

    private void validateStatusTransition(Status currentStatus, Status requiredStatus) {
        if (currentStatus.equals(requiredStatus)) {
            return;
        }
        ofNullable(StatusTransition.findTransition(currentStatus, requiredStatus))
                .orElseThrow(() -> new BadRequestException(String.format("Переход статуса из %s в %s невозможен", currentStatus, requiredStatus)));
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