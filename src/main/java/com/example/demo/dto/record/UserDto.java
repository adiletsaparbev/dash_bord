package com.example.demo.dto.record;

public record UserDto(
        Long id,
        String name,
        String surname,
        String email,
        String role,
        boolean active,
        DepartmentShortDto department
) {}

