package com.example.demo.dto.record;

public record MeDto(
        Long id,
        String name,
        String surname,
        String email,
        String role,
        Long departmentId,
        String departmentName
) {}