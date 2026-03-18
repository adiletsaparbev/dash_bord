package com.example.demo.dto.record;

public record UserUpdateDto(
        String name,
        String surname,
        String role,
        Long departmentId,
        Boolean active
) {}