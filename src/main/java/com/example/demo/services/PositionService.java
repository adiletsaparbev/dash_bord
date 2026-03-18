package com.example.demo.services;

import com.example.demo.entity.Position;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    @Transactional
    public Position createPosition(String name) {
        Position position = Position.builder()
                .name(name)
                .build();
        return positionRepository.save(position);
    }

    @Transactional
    public Position updatePosition(Long id, String name) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Должность не найдена"));
        position.setName(name);
        return positionRepository.save(position);
    }

    @Transactional
    public void deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Должность не найдена");
        }
        positionRepository.deleteById(id);
    }
}