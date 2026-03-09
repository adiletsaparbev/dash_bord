package com.example.demo.repositories;
// repositories/TaskAssigneeRepository.java


import com.example.demo.entity.TaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Long> {
    List<TaskAssignee> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
