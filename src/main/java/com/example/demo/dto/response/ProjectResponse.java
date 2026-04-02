package com.example.demo.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
<<<<<<< HEAD

    private Long pmId;
    private String pmName;

    private Long departmentId;
    private String departmentName;

    private List<Long> memberIds;
    private List<String> memberNames;

=======
    private String pmName;
    private String departmentName;
    private List<String> memberNames;
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    private int taskCount;
}