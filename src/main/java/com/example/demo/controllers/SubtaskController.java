package com.example.demo.controllers;

import com.example.demo.dto.request.SubtaskRequest;
import com.example.demo.entity.Subtask;
import com.example.demo.services.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @GetMapping
    public ResponseEntity<List<Subtask>> getAll(@PathVariable Long taskId) {
        return ResponseEntity.ok(subtaskService.getByTask(taskId));
    }

    @PostMapping
    public ResponseEntity<Subtask> create(@PathVariable Long taskId,
                                          @RequestBody SubtaskRequest request) {
        return ResponseEntity.ok(subtaskService.create(taskId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subtask> update(@PathVariable Long taskId,
                                          @PathVariable Long id,
                                          @RequestBody SubtaskRequest request) {
        return ResponseEntity.ok(subtaskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId,
                                       @PathVariable Long id) {
        subtaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}