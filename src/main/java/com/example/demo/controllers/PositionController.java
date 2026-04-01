package com.example.demo.controllers;

import com.example.demo.entity.Position;
import com.example.demo.services.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    // Доступно всем авторизованным пользователям
    @GetMapping
    public ResponseEntity<List<Position>> getAll() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    // Только для АДМИНА
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Position> create(@RequestBody String name) {
        return ResponseEntity.ok(positionService.createPosition(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Position> update(@PathVariable Long id, @RequestBody String name) {
        return ResponseEntity.ok(positionService.updatePosition(id, name));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}