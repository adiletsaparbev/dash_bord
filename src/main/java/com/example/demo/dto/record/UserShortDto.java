package com.example.demo.dto.record;

public record UserShortDto(
        Long id,
        String name,
        String surname,
        String email,
        String role,
        boolean active
) {}