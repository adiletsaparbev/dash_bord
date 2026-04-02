package com.example.demo.dto.record;

public record UserUpdateDto(
        String fullName,
        String role,
        Long departmentId,
<<<<<<< HEAD
        Boolean clearDepartment,
        Boolean active) {
}
=======
        Boolean active
) {}
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
