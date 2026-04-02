package com.example.demo.controllers;

import com.example.demo.dto.record.MeDto;
import com.example.demo.entity.Department;
import com.example.demo.entity.User;
<<<<<<< HEAD
import com.example.demo.enums.Role;
import com.example.demo.repositories.DepartmentRepository;
=======
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
import com.example.demo.repositories.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.transaction.annotation.Transactional;
=======
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
<<<<<<< HEAD
    private final DepartmentRepository departmentRepository;

    @GetMapping("/auth/me")
    @Transactional(readOnly = true)
    public ResponseEntity<MeDto> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User u = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // user.department есть у PM/TEAM.
        // Для MANAGER отдел хранится в department.manager_id → ищем через
        // findByManagerId.
        Department d = u.getDepartment();
        if (d == null && u.getRole() == Role.MANAGER) {
            d = departmentRepository.findFirstByManagerId(u.getId()).orElse(null);
        }

=======

    @GetMapping("/auth/me")
    public ResponseEntity<MeDto> me(Principal principal) {
        User u = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Department d = u.getDepartment();
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
        return ResponseEntity.ok(new MeDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name(),
                d != null ? d.getId() : null,
<<<<<<< HEAD
                d != null ? d.getName() : null));
=======
                d != null ? d.getName() : null
        ));
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    }
}
