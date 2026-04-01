package com.example.demo.dto.response;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String name;
    private String surname;
    private String avatarUrl;
    private String email;
    private String role;
    private String positionName;       // должность
    private String departmentName;     // отдел
    private boolean hasAvatar;

    private int totalTasks;            // количество задач
    private int activeTasks;           // активные (не DONE)
    private int overdueTasks;          // просроченные
    private double workloadPercent;    // процент загрузки
}