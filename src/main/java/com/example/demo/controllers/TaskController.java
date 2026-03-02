package com.example.demo.controllers;

import com.example.demo.dto.request.TaskRequest;
import com.example.demo.entity.Task;
import com.example.demo.enums.TaskStatus;
import com.example.demo.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Получить все задачи проекта
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    // Получить одну задачу
    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Создать задачу
    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.createTask(request, userDetails.getUsername()));
    }

    // Обновить задачу
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id,
                                       @Valid @RequestBody TaskRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, request, userDetails.getUsername()));
    }

    // Обновить только статус (Kanban drag&drop)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id,
                                             @RequestParam TaskStatus status,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateStatus(id, status, userDetails.getUsername()));
    }

    // Удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // Поиск по названию
    @GetMapping("/project/{projectId}/search")
    public ResponseEntity<List<Task>> search(@PathVariable Long projectId,
                                             @RequestParam String keyword) {
        return ResponseEntity.ok(taskService.searchTasks(projectId, keyword));
    }

    // Фильтр по статусу
    @GetMapping("/project/{projectId}/filter")
    public ResponseEntity<List<Task>> filter(@PathVariable Long projectId,
                                             @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.filterByStatus(projectId, status));
    }
}