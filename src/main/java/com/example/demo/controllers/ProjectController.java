package com.example.demo.controllers;

import com.example.demo.dto.request.ProjectRequest;
import com.example.demo.entity.Project;
import com.example.demo.services.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
    public ResponseEntity<List<Project>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getAll(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
    public ResponseEntity<Project> getById(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getById(id, userDetails.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Project> create(
            @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.create(request, userDetails.getUsername()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody ProjectRequest request,  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
