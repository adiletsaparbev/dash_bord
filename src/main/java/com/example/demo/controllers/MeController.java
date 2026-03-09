package com.example.demo.controllers;

import com.example.demo.dto.record.MeDto;
import com.example.demo.entity.Department;
import com.example.demo.entity.User;
import com.example.demo.repositories.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/auth/me")
    public ResponseEntity<MeDto> me(Principal principal) {
        User u = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Department d = u.getDepartment();
        return ResponseEntity.ok(new MeDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name(),
                d != null ? d.getId() : null,
                d != null ? d.getName() : null
        ));
    }
}
