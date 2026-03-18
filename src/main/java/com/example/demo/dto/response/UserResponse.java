package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String role;
    private boolean active;
    private String departmentName;
    private String positionName;
    private boolean hasAvatar;
    private LocalDateTime createdAt;
}