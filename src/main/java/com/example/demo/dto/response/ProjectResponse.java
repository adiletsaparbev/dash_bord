package com.example.demo.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String pmName;
    private String departmentName;
    private List<String> memberNames;
    private int taskCount;
}