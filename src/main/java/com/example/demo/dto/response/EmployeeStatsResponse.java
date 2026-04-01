package com.example.demo.dto.response;

import lombok.Data;

@Data
public class EmployeeStatsResponse {
    private Long userId;
    private String name;
    private String surname;

    private int totalTasks;            // всего задач
    private int completedTasks;        // завершённые
    private int activeTasks;           // активные
    private int overdueTasks;          // просроченные
    private double overduePercent;     // процент просрочек
    private Double avgCompletionHours; // среднее время выполнения (в часах)
}