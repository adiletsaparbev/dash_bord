package com.example.demo.dto.request;

import com.example.demo.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubtaskRequest {
    private String title;
    private Long assigneeId;
    private TaskStatus status = TaskStatus.NEW;
    private LocalDate dueDate;
}