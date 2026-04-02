package com.example.demo.controllers;

import com.example.demo.dto.request.ProjectRequest;
<<<<<<< HEAD
import com.example.demo.dto.response.ProjectResponse;
=======
import com.example.demo.entity.Project;
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
import com.example.demo.services.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD

=======
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
<<<<<<< HEAD
    public ResponseEntity<List<ProjectResponse>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getAllAsResponse(userDetails.getUsername()));
=======
    public ResponseEntity<List<Project>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getAll(userDetails.getUsername()));
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM','TEAM')")
<<<<<<< HEAD
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.getByIdAsResponse(id, userDetails.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','PM')")
    public ResponseEntity<ProjectResponse> create(
            @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.createAsResponse(request, userDetails.getUsername()));
=======
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
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
<<<<<<< HEAD
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.updateAsResponse(id, request, userDetails.getUsername()));
=======
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody ProjectRequest request,  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.update(id, request, userDetails.getUsername()));
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
