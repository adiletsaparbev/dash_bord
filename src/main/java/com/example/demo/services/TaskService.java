package com.example.demo.services;

import com.example.demo.dto.request.TaskRequest;
import com.example.demo.entity.*;
import com.example.demo.enums.TaskStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskHistoryRepository historyRepository;
    private final NotificationService notificationService;

    // CREATE
    public Task createTask(TaskRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .creator(creator)
                .status(request.getStatus())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);

        // Назначить исполнителей
        if (request.getAssigneeIds() != null) {
            for (Long userId : request.getAssigneeIds()) {
                User assignee = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Исполнитель не найден: " + userId));
                // Уведомление исполнителю
                notificationService.send(assignee, "TASK_ASSIGNED",
                        "Вам назначена задача: " + task.getTitle(), task);
            }
        }

        return task;
    }

    // READ ALL by project
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // READ ONE
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + id));
    }

    // UPDATE
    public Task updateTask(Long id, TaskRequest request, String editorEmail) {
        Task task = getTaskById(id);
        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Записываем историю изменений
        if (!task.getStatus().name().equals(request.getStatus().name())) {
            saveHistory(task, editor, "status", task.getStatus().name(), request.getStatus().name());
            // Уведомить исполнителей об изменении статуса
            task.getAssignees().forEach(a ->
                    notificationService.send(a.getUser(), "STATUS_CHANGED",
                            "Статус задачи изменён: " + task.getTitle(), task));
        }
        if (!task.getPriority().name().equals(request.getPriority().name())) {
            saveHistory(task, editor, "priority", task.getPriority().name(), request.getPriority().name());
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return taskRepository.save(task);
    }

    // UPDATE STATUS only (для drag&drop в Kanban)
    public Task updateStatus(Long id, TaskStatus newStatus, String editorEmail) {
        Task task = getTaskById(id);
        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        saveHistory(task, editor, "status", task.getStatus().name(), newStatus.name());
        task.setStatus(newStatus);

        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(java.time.LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    // DELETE
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    // Search
    public List<Task> searchTasks(Long projectId, String keyword) {
        return taskRepository.searchByTitle(projectId, keyword);
    }

    // Filter by status
    public List<Task> filterByStatus(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status);
    }

    private void saveHistory(Task task, User editor, String field, String oldVal, String newVal) {
        historyRepository.save(TaskHistory.builder()
                .task(task)
                .changedBy(editor)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .build());
    }
}