package com.example.demo.services;

import com.example.demo.dto.request.TaskRequest;
import com.example.demo.dto.request.TaskUpdateRequest;
import com.example.demo.dto.response.TaskResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final SubtaskRepository subtaskRepository;
    private final TagRepository tagRepository;
    private final NotificationService notificationService;
    private final TaskHistoryRepository taskHistoryRepository;
    private final EmployeeService employeeService;

    private void checkWorkloadForUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        for (Long userId : userIds) {
            userRepository.findById(userId).ifPresent(user -> {
                double currentWorkload = employeeService.calculateWorkload(user.getId());
                if (currentWorkload >= 90.0) {
                    employeeService.sendOverloadNotificationIfNeeded(user, currentWorkload);
                }
            });
        }
    }

    // =========================================================
    // 1. Список задач по ролям
    // =========================================================
    public List<TaskResponse> getAllTasks(String email) {
        User user = getUser(email);
        Role role = user.getRole();
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(task -> hasAccess(task, user, role))
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // 2. Поиск по названию и/или тегам
    // =========================================================
    public List<TaskResponse> searchTasks(String title, List<Long> tagIds, String email) {
        User user = getUser(email);
        Role role = user.getRole();

        String filterTitle = (title != null && title.trim().isEmpty()) ? null : title;
        List<Long> filterTagIds = (tagIds != null && tagIds.isEmpty()) ? null : tagIds;

        List<Task> tasks = taskRepository.searchTasks(filterTitle, filterTagIds);
        return tasks.stream()
                .filter(task -> hasAccess(task, user, role))
                .map(this::toResponse)
                .toList();
    }

    private boolean hasAccess(Task task, User user, Role role) {
        if (role == Role.ADMIN) return true;
        if (role == Role.MANAGER) {
            Department department = task.getProject().getDepartment();
            if (department == null || department.getManager() == null) return false;
            return department.getManager().getId().equals(user.getId());
        }
        if (role == Role.PM) {
            Project project = task.getProject();
            return project != null && project.getPm() != null
                    && project.getPm().getId().equals(user.getId());
        }
        if (role == Role.TEAM) {
            return task.getAssignees().stream()
                    .anyMatch(a -> a.getUser().getId().equals(user.getId()));
        }
        return false;
    }

    // =========================================================
    // 3. Создание задачи
    // =========================================================
    public TaskResponse createTask(TaskRequest request, String email) {
        User currentUser = getUser(email);
        validateCreateRequest(request);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));

        if (currentUser.getRole() == Role.MANAGER) {
            if (!hasDepartmentAndManager(project) ||
                    !project.getDepartment().getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "Руководитель может создавать задачи только в проектах своего отдела");
            }
        }
        if (currentUser.getRole() == Role.PM) {
            if (project.getPm() == null || !project.getPm().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "PM может создавать задачи только в проектах, где он назначен менеджером проекта");
            }
        }

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .project(project)
                .creator(currentUser)
                .status(TaskStatus.NEW)
                .estimatedHours(request.getEstimatedHours())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .startDate(request.getStartDate())
                .complexity(request.getComplexity() != null ? request.getComplexity() : Complexity.LOW)
                .dueDate(request.getDueDate())
                .tags(new HashSet<>())
                .assignees(new ArrayList<>())
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            if (tags.size() != request.getTagIds().size()) {
                throw new ResourceNotFoundException("Один или несколько тегов не найдены");
            }
            task.getTags().addAll(tags);
        }

        for (Long assigneeId : request.getAssigneeIds()) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Исполнитель не найден: " + assigneeId));

            if (currentUser.getRole() == Role.MANAGER) {
                if (assignee.getRole() != Role.PM && assignee.getRole() != Role.TEAM) {
                    throw new AccessDeniedException(
                            "Руководитель может назначать только PM и TEAM");
                }
                if (assignee.getDepartment() == null || project.getDepartment() == null
                        || !assignee.getDepartment().getId()
                        .equals(project.getDepartment().getId())) {
                    throw new AccessDeniedException(
                            "Исполнитель должен принадлежать тому же отделу, что и проект");
                }
            }

            task.getAssignees().add(TaskAssignee.builder()
                    .task(task).user(assignee).build());
        }

        Task saved = taskRepository.save(task);

        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments =
                    attachmentRepository.findAllById(request.getAttachmentIds());
            for (Attachment att : attachments) {
                att.setEntityType(EntityType.TASK);
                att.setEntityId(saved.getId());
            }
            attachmentRepository.saveAll(attachments);
        }

        if (request.getSubtasks() != null && !request.getSubtasks().isEmpty()) {
            List<Subtask> subtaskList = request.getSubtasks().stream().map(dto -> {
                Subtask st = new Subtask();
                st.setTitle(dto.getTitle());
                st.setTask(saved);
                return st;
            }).toList();
            subtaskRepository.saveAll(subtaskList);
        }

        // === Уведомление: Новая задача назначена ===
        saved.getAssignees().forEach(ta ->
                notificationService.send(
                        ta.getUser(),
                        "TASK_ASSIGNED",
                        "Вам назначена новая задача: \"" + saved.getTitle() + "\"",
                        saved
                )
        );

        // === История: создание задачи ===
        recordHistory(saved, currentUser, "created",
                null, "Задача создана: \"" + saved.getTitle() + "\"");
        taskRepository.flush();

        // === Проверка перегрузки ПОСЛЕ сохранения задачи ===
        checkWorkloadForUsers(request.getAssigneeIds());

        return toResponse(saved);
    }

    // =========================================================
    // 4. Получение одной задачи
    //    При открытии — автоматически читаем уведомления по задаче
    // =========================================================
    public TaskResponse getTaskById(Long taskId, String email) {
        User user = getUser(email);
        Task task = findTask(taskId);
        validateTaskAccess(user, task, "VIEW");

        notificationService.markTaskNotificationsAsRead(taskId, user.getId());

        return toResponse(task);
    }

    // =========================================================
    // 5. Обновление задачи — пишем историю каждого изменения
    // =========================================================
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, String email) {
        User user = getUser(email);
        Task task = findTask(taskId);
        validateTaskAccess(user, task, "EDIT");

        // --- Название ---
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()
                && !request.getTitle().trim().equals(task.getTitle())) {
            recordHistory(task, user, "title", task.getTitle(), request.getTitle().trim());
            task.setTitle(request.getTitle().trim());
        }

        // --- Описание ---
        if (request.getDescription() != null
                && !Objects.equals(request.getDescription(), task.getDescription())) {
            recordHistory(task, user, "description", task.getDescription(), request.getDescription());
            task.setDescription(request.getDescription());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        // --- Приоритет ---
        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            recordHistory(task, user, "priority",
                    task.getPriority() != null ? task.getPriority().name() : null,
                    request.getPriority().name());
            task.setPriority(request.getPriority());
        }

        // --- Дата начала ---
        if (request.getStartDate() != null
                && !Objects.equals(request.getStartDate(), task.getStartDate())) {
            recordHistory(task, user, "startDate",
                    String.valueOf(task.getStartDate()), String.valueOf(request.getStartDate()));
            task.setStartDate(request.getStartDate());
        }

        // --- Срок выполнения ---
        if (request.getDueDate() != null
                && !Objects.equals(request.getDueDate(), task.getDueDate())) {
            if (request.getDueDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Срок выполнения должен быть >= текущей даты");
            }
            recordHistory(task, user, "dueDate",
                    String.valueOf(task.getDueDate()), String.valueOf(request.getDueDate()));
            task.setDueDate(request.getDueDate());
        }

        // --- Статус ---
        if (request.getStatus() != null) {
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.PM) {
                if (request.getStatus() != task.getStatus()) {
                    String oldStatus = task.getStatus() != null ? task.getStatus().name() : null;
                    String newStatus = request.getStatus().name();

                    recordHistory(task, user, "status", oldStatus, newStatus);
                    task.setStatus(request.getStatus());

                    if (request.getStatus() == TaskStatus.DONE) {
                        task.setCompletedAt(java.time.LocalDateTime.now());
                        task.setOverdue(false);
                    }

                    // === Уведомление: Изменился статус ===
                    String msg = "Статус задачи \"" + task.getTitle() + "\" изменён: "
                            + oldStatus + " → " + newStatus;
                    notificationService.notifyTaskParticipants(task, "STATUS_CHANGED", msg, user);
                }
            } else {
                throw new AccessDeniedException("Менять статус задачи может только ADMIN или PM");
            }
        }

        // --- Исполнители ---
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {

                String oldAssignees = task.getAssignees() != null
                        ? task.getAssignees().stream()
                        .map(a -> a.getUser().getEmail())
                        .reduce((a, b) -> a + ", " + b).orElse("")
                        : "";

                task.getAssignees().clear();

                for (Long assigneeId : request.getAssigneeIds()) {
                    User assignee = userRepository.findById(assigneeId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Исполнитель не найден: " + assigneeId));

                    if (user.getRole() == Role.MANAGER) {
                        if (assignee.getRole() != Role.PM && assignee.getRole() != Role.TEAM) {
                            throw new AccessDeniedException(
                                    "Руководитель может назначать только PM и TEAM");
                        }
                        if (task.getProject().getDepartment() == null
                                || assignee.getDepartment() == null
                                || !task.getProject().getDepartment().getId()
                                .equals(assignee.getDepartment().getId())) {
                            throw new AccessDeniedException(
                                    "Исполнитель должен быть из того же отдела");
                        }
                    }

                    task.getAssignees().add(TaskAssignee.builder()
                            .task(task).user(assignee).build());
                }

                // === Сначала сохраняем, потом проверяем загрузку ===
                taskRepository.save(task);

                taskRepository.flush();
                // === Проверка перегрузки ПОСЛЕ сохранения ===
                checkWorkloadForUsers(request.getAssigneeIds());

                String newAssignees = task.getAssignees().stream()
                        .map(a -> a.getUser().getEmail())
                        .reduce((a, b) -> a + ", " + b).orElse("");

                recordHistory(task, user, "assignees", oldAssignees, newAssignees);

                // === Уведомление: Назначен новый исполнитель ===
                task.getAssignees().forEach(ta ->
                        notificationService.send(
                                ta.getUser(),
                                "TASK_ASSIGNED",
                                "Вы назначены исполнителем задачи: \"" + task.getTitle() + "\"",
                                task
                        )
                );

            } else {
                throw new AccessDeniedException("У вас нет прав на изменение исполнителей");
            }
        }

        return toResponse(taskRepository.save(task));
    }

    // =========================================================
    // 6. Удаление задачи
    // =========================================================
    public void deleteTask(Long taskId, String email) {
        User user = getUser(email);
        Task task = findTask(taskId);

        if (user.getRole() == Role.ADMIN) {
            taskRepository.delete(task);
            return;
        }

        if (user.getRole() == Role.MANAGER) {
            if (!hasDepartmentAndManager(task.getProject()) ||
                    !task.getProject().getDepartment().getManager().getId().equals(user.getId())) {
                throw new AccessDeniedException("Нет доступа к задачам другого отдела");
            }
            taskRepository.delete(task);
            return;
        }

        throw new AccessDeniedException("У вас нет прав на удаление задачи");
    }

    // =========================================================
    // Запись истории изменений
    // =========================================================
    private void recordHistory(Task task, User changedBy,
                               String fieldName, String oldValue, String newValue) {
        if (Objects.equals(oldValue, newValue)) return;

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changedBy(changedBy)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        taskHistoryRepository.save(history);
    }

    // =========================================================
    // Проверка доступа к задаче
    // =========================================================
    private void validateTaskAccess(User user, Task task, String action) {
        Role role = user.getRole();
        if (role == Role.ADMIN) return;

        if (role == Role.MANAGER) {
            if (!hasDepartmentAndManager(task.getProject()) ||
                    !task.getProject().getDepartment().getManager().getId().equals(user.getId())) {
                throw new AccessDeniedException("Нет доступа к задачам другого отдела");
            }
            if ("EDIT".equals(action)) {
                if (task.getCreator() == null ||
                        !task.getCreator().getId().equals(user.getId())) {
                    throw new AccessDeniedException(
                            "Руководитель может редактировать только свои задачи");
                }
            }
            return;
        }

        if (role == Role.PM) {
            if (task.getProject() == null || task.getProject().getPm() == null
                    || !task.getProject().getPm().getId().equals(user.getId())) {
                throw new AccessDeniedException(
                        "ПМ видит и редактирует только задачи своего проекта");
            }
            return;
        }

        if (role == Role.TEAM) {
            boolean assigned = task.getAssignees() != null &&
                    task.getAssignees().stream()
                            .anyMatch(a -> a.getUser() != null
                                    && a.getUser().getId().equals(user.getId()));
            if (!assigned) {
                throw new AccessDeniedException(
                        "Команда может видеть только задачи, где она назначена");
            }
            if (!"VIEW".equals(action)) {
                throw new AccessDeniedException("У роли TEAM нет прав на изменение задач");
            }
            return;
        }

        throw new AccessDeniedException("Недостаточно прав");
    }

    // =========================================================
    // Валидация запроса на создание
    // =========================================================
    private void validateCreateRequest(TaskRequest request) {
        if (request.getProjectId() == null)
            throw new IllegalArgumentException("projectId обязателен");
        if (request.getTitle() == null || request.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Название задачи обязательно");
        if (request.getDueDate() == null)
            throw new IllegalArgumentException("Срок выполнения обязателен");
        if (request.getDueDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Срок выполнения должен быть >= текущей даты");
        if (request.getAssigneeIds() == null || request.getAssigneeIds().isEmpty())
            throw new IllegalArgumentException("Исполнитель должен быть выбран");
    }

    private boolean hasDepartmentAndManager(Project project) {
        return project != null
                && project.getDepartment() != null
                && project.getDepartment().getManager() != null;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        r.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        r.setStartDate(task.getStartDate());
        r.setDueDate(task.getDueDate());
        r.setOverdue(task.isOverdue());
        r.setCompletedAt(task.getCompletedAt());
        r.setCreatedAt(task.getCreatedAt());
        r.setEstimatedHours(task.getEstimatedHours());
        r.setActualHours(task.getActualHours());

        if (task.getProject() != null) {
            r.setProjectId(task.getProject().getId());
            r.setProjectName(task.getProject().getName());
            if (task.getProject().getPm() != null) {
                r.setPmId(task.getProject().getPm().getId());
                r.setPmName(task.getProject().getPm().getName() + " " + task.getProject().getPm().getSurname());
            }
        }

        if (task.getCreator() != null) {
            r.setCreatorId(task.getCreator().getId());
            r.setCreatorName(task.getCreator().getName() + " " + task.getCreator().getSurname());
        }

        if (task.getAssignees() != null) {
            r.setAssigneeIds(task.getAssignees().stream()
                    .map(a -> a.getUser().getId()).toList());
            r.setAssigneeNames(task.getAssignees().stream()
                    .map(a -> a.getUser().getEmail()).toList());
        }

        if (task.getTags() != null && !task.getTags().isEmpty()) {
            r.setTagNames(task.getTags().stream().map(Tag::getName).toList());
        }

        if (task.getSubtasks() != null) {
            r.setSubtaskCount(task.getSubtasks().size());
            r.setCompletedSubtaskCount((int) task.getSubtasks().stream()
                    .filter(s -> s.getStatus() == TaskStatus.DONE).count());
        }

        return r;
    }
}
