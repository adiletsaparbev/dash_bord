package com.example.demo.services;

import com.example.demo.dto.request.SubtaskRequest;
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
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Subtask create(Long taskId, SubtaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        }

        Subtask subtask = Subtask.builder()
                .task(task)
                .title(request.getTitle())
                .assignee(assignee)
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .build();

        return subtaskRepository.save(subtask);
    }

    public List<Subtask> getByTask(Long taskId) {
        return subtaskRepository.findByTaskId(taskId);
    }

    public Subtask update(Long id, SubtaskRequest request) {
        Subtask subtask = subtaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Подзадача не найдена"));

        subtask.setTitle(request.getTitle());
        subtask.setStatus(request.getStatus());
        subtask.setDueDate(request.getDueDate());

        Subtask saved = subtaskRepository.save(subtask);

        // Проверяем: если все подзадачи DONE → обновляем статус родительской задачи
        checkAndCompleteParentTask(subtask.getTask());

        return saved;
    }

    public void delete(Long id) {
        subtaskRepository.deleteById(id);
    }

    private void checkAndCompleteParentTask(Task task) {
        long total = subtaskRepository.countByTaskId(task.getId());
        long done = subtaskRepository.countByTaskIdAndStatus(task.getId(), TaskStatus.DONE);

        if (total > 0 && total == done) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(java.time.LocalDateTime.now());
            taskRepository.save(task);
        }
    }
}