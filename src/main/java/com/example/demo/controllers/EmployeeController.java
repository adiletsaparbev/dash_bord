package com.example.demo.controllers;

import com.example.demo.dto.response.EmployeeResponse;
import com.example.demo.dto.response.EmployeeStatsResponse;
import com.example.demo.entity.Image;
import com.example.demo.services.EmployeeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // GET /api/employees
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                employeeService.getAllEmployees(userDetails.getUsername())
        );
    }

    // GET /api/employees/filter?departmentId=1&role=TEAM&workload=overloaded&projectId=2
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> filterEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String workload,
            @RequestParam(required = false) Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                employeeService.filterEmployees(
                        userDetails.getUsername(),
                        departmentId,
                        role,
                        workload,
                        projectId
                )
        );
    }

    // GET /api/employees/{id}/stats
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
    public ResponseEntity<EmployeeStatsResponse> getStats(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                employeeService.getEmployeeStats(id, userDetails.getUsername())
        );
    }

    // GET /api/employee/{id}/avatar — получить аватар сотрудника в байтах
    @GetMapping("/{id}/avatar")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) {
        Image avatar = employeeService.getEmployeeAvatar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .body(avatar.getBytes());
    }
}