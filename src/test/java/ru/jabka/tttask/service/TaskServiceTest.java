package ru.jabka.tttask.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.jabka.tttask.client.UserClient;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Status;
import ru.jabka.tttask.model.Task;
import ru.jabka.tttask.model.TaskRequest;
import ru.jabka.tttask.model.UpdateTask;
import ru.jabka.tttask.model.UserResponse;
import ru.jabka.tttask.model.UserRole;
import ru.jabka.tttask.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private TaskService taskService;

    @Test
    void create_success() {
        final TaskRequest taskRequest = getValidTaskRequest();
        final Task task = Task.builder()
                .title(taskRequest.title())
                .description(taskRequest.description())
                .status(Status.TO_DO)
                .deadLine(taskRequest.deadLine())
                .author(taskRequest.author())
                .assignee(taskRequest.assignee())
                .build();
        Set<Long> members = Set.of(taskRequest.assignee(), taskRequest.author());
        Mockito.when(userClient.getAllByIds(members))
                .thenReturn(idSetToUserResponseSet(members));
        Mockito.when(taskRepository.insert(task)).thenReturn(task);
        Task result = taskService.create(taskRequest);
        Assertions.assertEquals(task, result);
        Mockito.verify(taskRepository).insert(task);
        Mockito.verify(userClient).getAllByIds(members);
    }

    @Test
    void update_success_manager() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .assignee(3L)
                .status(Status.TO_DO)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(updateTask.status())
                .build();
        Set<Long> members = Set.of(updateTask.assignee());
        UserResponse assignee = UserResponse.builder()
                .id(updateTask.assignee())
                .build();
        Mockito.when(userClient.getAllByIds(members)).thenReturn(Set.of(assignee));
        Mockito.when(taskRepository.update(task)).thenReturn(task);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(task, result);
        Mockito.verify(taskRepository).update(task);
        Mockito.verify(userClient).getAllByIds(members);
    }

    @Test
    void update_success_user() {
        UserResponse user = mockUser();
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .status(Status.TO_DO)
                .editor(user.id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(updateTask.status())
                .build();
        Set<Long> members = Set.of(user.id());
        Mockito.when(taskRepository.update(task)).thenReturn(task);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(task, result);
        Mockito.verify(taskRepository).update(task);
        Mockito.verify(userClient).getAllByIds(members);
    }

    @Test
    void update_success_todo_to_in_progress() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.IN_PROGRESS)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.TO_DO)
                .build();
        final Task updated = Task.builder()
                .id(task.id())
                .title(task.title())
                .description(task.description())
                .deadLine(task.deadLine())
                .assignee(task.assignee())
                .status(updateTask.status())
                .build();
        Mockito.when(taskRepository.update(updated)).thenReturn(updated);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(updated, result);
        Mockito.verify(taskRepository).update(updated);
    }

    @Test
    void update_success_todo_to_deleted() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DELETED)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.TO_DO)
                .build();
        final Task updated = Task.builder()
                .id(task.id())
                .title(task.title())
                .description(task.description())
                .deadLine(task.deadLine())
                .assignee(task.assignee())
                .status(updateTask.status())
                .build();
        Mockito.when(taskRepository.update(updated)).thenReturn(updated);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(updated, result);
        Mockito.verify(taskRepository).update(updated);
    }

    @Test
    void update_success_in_progress_to_done() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DONE)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.IN_PROGRESS)
                .build();
        final Task updated = Task.builder()
                .id(task.id())
                .title(task.title())
                .description(task.description())
                .deadLine(task.deadLine())
                .assignee(task.assignee())
                .status(updateTask.status())
                .build();
        Mockito.when(taskRepository.update(updated)).thenReturn(updated);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(updated, result);
        Mockito.verify(taskRepository).update(updated);
    }

    @Test
    void update_success_in_progress_to_deleted() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DELETED)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.IN_PROGRESS)
                .build();
        final Task updated = Task.builder()
                .id(task.id())
                .title(task.title())
                .description(task.description())
                .deadLine(task.deadLine())
                .assignee(task.assignee())
                .status(updateTask.status())
                .build();
        Mockito.when(taskRepository.update(updated)).thenReturn(updated);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(updated, result);
        Mockito.verify(taskRepository).update(updated);
    }

    @Test
    void update_success_done_to_deleted() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DELETED)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DONE)
                .build();
        final Task updated = Task.builder()
                .id(task.id())
                .title(task.title())
                .description(task.description())
                .deadLine(task.deadLine())
                .assignee(task.assignee())
                .status(updateTask.status())
                .build();
        Mockito.when(taskRepository.update(updated)).thenReturn(updated);
        Mockito.when(taskRepository.getById(updateTask.id())).thenReturn(task);
        Task result = taskService.update(updateTask);
        Assertions.assertEquals(updated, result);
        Mockito.verify(taskRepository).update(updated);
    }

    @Test
    void getById_success() {
        final Long taskId = 1L;
        final Task task = getValidTask();
        Mockito.when(taskRepository.getById(taskId)).thenReturn(task);
        Task result = taskService.getById(taskId);
        Assertions.assertEquals(task, result);
        Mockito.verify(taskRepository).getById(taskId);
    }

    @Test
    void getByAssigneeId_success() {
        final Task task = getValidTask();
        final Long assignee = task.assignee();
        List<Task> taskList = List.of(task);
        Mockito.when(taskRepository.getByAssigneeId(assignee)).thenReturn(taskList);
        List<Task> result = taskService.getByAssigneeId(assignee);
        Assertions.assertEquals(taskList, result);
        Mockito.verify(taskRepository).getByAssigneeId(assignee);
    }

    @Test
    void getAllByFilter_success_fullFilter() {
        final Status status = Status.TO_DO;
        final Long assignee = 1L;
        final List<Task> taskList = List.of(getValidTask());
        Set<Long> ids = Set.of(assignee);
        Mockito.when(userClient.getAllByIds(ids)).thenReturn(idSetToUserResponseSet(ids));
        Mockito.when(taskRepository.getByFilter(status.getId(), assignee)).thenReturn(taskList);
        Set<Task> result = taskService.getAllByFilter(status, assignee);
        Assertions.assertEquals(Set.copyOf(taskList), result);
        Mockito.verify(taskRepository).getByFilter(status.getId(), assignee);
        Mockito.verify(userClient).getAllByIds(ids);
    }

    @Test
    void getAllByFilter_success_onlyStatus() {
        final Status status = Status.TO_DO;
        final List<Task> taskList = List.of(getValidTask());
        Mockito.when(taskRepository.getByFilter(status.getId(), null)).thenReturn(taskList);
        Set<Task> result = taskService.getAllByFilter(status, null);
        Assertions.assertEquals(Set.copyOf(taskList), result);
        Mockito.verify(taskRepository).getByFilter(status.getId(), null);
        Mockito.verify(userClient, Mockito.never()).getAllByIds(Mockito.any());
    }

    @Test
    void getAllByFilter_success_onlyAssignee() {
        final Long assignee = 1L;
        final List<Task> taskList = List.of(getValidTask());
        Set<Long> ids = Set.of(assignee);
        Mockito.when(userClient.getAllByIds(ids)).thenReturn(idSetToUserResponseSet(ids));
        Mockito.when(taskRepository.getByFilter(null, assignee)).thenReturn(taskList);
        Set<Task> result = taskService.getAllByFilter(null, assignee);
        Assertions.assertEquals(Set.copyOf(taskList), result);
        Mockito.verify(taskRepository).getByFilter(null, assignee);
        Mockito.verify(userClient).getAllByIds(ids);
    }

    @Test
    void create_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(null)
        );
        Assertions.assertEquals("Заполните данные задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullTitle() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title(null)
                .description("description")
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Заполните заголовок задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullDescription() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description(null)
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Заполните описание задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullDeadline() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(null)
                .author(1L)
                .assignee(2L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Заполните срок исполнения задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_deadlineBeforeNow() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.of(2019, 12, 10))
                .author(1L)
                .assignee(2L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Срок исполнения не может быть в прошлом", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullAuthor() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .author(null)
                .assignee(2L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Заполните автора задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullAssignee() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals("Заполните исполнителя задачи", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_authorNotExists() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .build();
        Set<Long> members = Set.of(taskRequest.assignee(), taskRequest.author());
        Mockito.when(userClient.getAllByIds(members))
                .thenReturn(Set.of(UserResponse.builder()
                        .id(taskRequest.assignee())
                        .build()));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals(
                String.format("Пользователь с id = %d не найден", taskRequest.author()),
                exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_assigneeNotExists() {
        final TaskRequest taskRequest = TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .build();
        Set<Long> members = Set.of(taskRequest.assignee(), taskRequest.author());
        Mockito.when(userClient.getAllByIds(members))
                .thenReturn(Set.of(UserResponse.builder()
                        .id(taskRequest.author())
                        .build()));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.create(taskRequest)
        );
        Assertions.assertEquals(
                String.format("Пользователь с id = %d не найден", taskRequest.assignee()),
                exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void update_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(null)
        );
        Assertions.assertEquals("Заполните данные для обновления", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_nullTaskId() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(null)
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .assignee(2L)
                .status(Status.TO_DO)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals("Не указан id задачи, которую необходимо обновить", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_assigneeNotExists() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .assignee(2L)
                .status(Status.TO_DO)
                .editor(mockManager().id())
                .build();
        Set<Long> members = Set.of(updateTask.assignee());
        Mockito.when(userClient.getAllByIds(members)).thenReturn(Set.of());
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(
                String.format("Пользователь с id = %d не найден", updateTask.assignee()),
                exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_deadlineBeforeNow() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .title("Title")
                .description("description")
                .deadLine(LocalDate.of(2019, 12, 10))
                .assignee(2L)
                .status(Status.TO_DO)
                .editor(mockManager().id())
                .build();
        Set<Long> members = Set.of(updateTask.assignee());
        Mockito.when(userClient.getAllByIds(members))
                .thenReturn(idSetToUserResponseSet(members));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals("Срок исполнения не может быть в прошлом", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_todo_to_done_status() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DONE)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.TO_DO)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_in_progress_to_todo() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.TO_DO)
                .editor(mockManager().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.IN_PROGRESS)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_done_to_todo() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.TO_DO)
                .editor(mockUser().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DONE)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_done_to_in_progress() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.IN_PROGRESS)
                .editor(mockUser().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DONE)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_deleted_to_todo() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.TO_DO)
                .editor(mockUser().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DELETED)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_deleted_to_in_progress() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.IN_PROGRESS)
                .editor(mockUser().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DELETED)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_deleted_to_done() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .status(Status.DONE)
                .editor(mockUser().id())
                .build();
        final Task task = Task.builder()
                .id(updateTask.id())
                .title(updateTask.title())
                .description(updateTask.description())
                .deadLine(updateTask.deadLine())
                .assignee(updateTask.assignee())
                .status(Status.DELETED)
                .build();
        Mockito.when(taskRepository.getById(task.id())).thenReturn(task);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Переход статуса из %s в %s невозможен", task.status(), updateTask.status()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_user_permission() {
        Long editor = mockUser().id();
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .assignee(3L)
                .editor(editor)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Роль пользователя id = %d, выполняющего редактирование, не соответствует роли %s", editor, UserRole.MANAGER), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_null_editor() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .assignee(3L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals("Не указан id пользователя, который редактирует задачу", exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_editor_not_found() {
        final UpdateTask updateTask = UpdateTask.builder()
                .id(1L)
                .description("123")
                .editor(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> taskService.update(updateTask)
        );
        Assertions.assertEquals(String.format("Пользователь с id = %d, выполняющий редактирование, не найден", updateTask.editor()), exception.getMessage());
        Mockito.verify(taskRepository, Mockito.never()).update(Mockito.any());
    }

    private TaskRequest getValidTaskRequest() {
        return TaskRequest.builder()
                .title("Title")
                .description("description")
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .build();
    }

    private Task getValidTask() {
        return Task.builder()
                .id(1L)
                .title("Title")
                .description("description")
                .status(Status.TO_DO)
                .deadLine(LocalDate.now())
                .author(1L)
                .assignee(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Set<UserResponse> idSetToUserResponseSet(final Set<Long> ids) {
        return ids.stream()
                .map(x -> UserResponse.builder()
                        .id(x)
                        .build())
                .collect(Collectors.toSet());
    }

    private UserResponse mockUser() {
        UserResponse userResponse = UserResponse.builder()
                .id(8L)
                .role(UserRole.USER)
                .build();
        Mockito.when(userClient.getAllByIds(Set.of(userResponse.id()))).thenReturn(Set.of(userResponse));
        return userResponse;
    }

    private UserResponse mockManager() {
        UserResponse userResponse = UserResponse.builder()
                .id(7L)
                .role(UserRole.MANAGER)
                .build();
        Mockito.when(userClient.getAllByIds(Set.of(userResponse.id()))).thenReturn(Set.of(userResponse));
        return userResponse;
    }
}