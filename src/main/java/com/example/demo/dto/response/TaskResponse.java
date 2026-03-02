package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private boolean isOverdue;
    private String pmName;
    private List<String> assigneeNames;
    private List<String> tagNames;
    private int subtaskCount;
    private int completedSubtaskCount;
    private LocalDateTime createdAt;
}
