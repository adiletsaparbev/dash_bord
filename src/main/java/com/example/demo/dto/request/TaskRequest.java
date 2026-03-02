package com.example.demo.dto.request;

import com.example.demo.enums.Priority;
import com.example.demo.enums.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskRequest {

    @NotBlank(message = "Название задачи обязательно")
    private String title;

    private String description;

    @NotNull(message = "Проект обязателен")
    private Long projectId;

    @NotNull(message = "Укажите хотя бы одного исполнителя")
    private List<Long> assigneeIds;

    private Long pmId;
    private Priority priority = Priority.MEDIUM;
    private TaskStatus status = TaskStatus.NEW;
    private LocalDate startDate;

    @Future(message = "Срок должен быть в будущем")
    private LocalDate dueDate;

    private List<Long> tagIds;
}