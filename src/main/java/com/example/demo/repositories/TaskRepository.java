package com.example.demo.repositories;

import com.example.demo.entity.Task;
import com.example.demo.enums.Priority;
import com.example.demo.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findByProjectIdAndPriority(Long projectId, Priority priority);

    @Query("SELECT t FROM Task t JOIN t.assignees a WHERE a.user.id = :userId")
    List<Task> findByAssigneeId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND t.isOverdue = true")
    List<Task> findOverdueTasks(Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByTitle(Long projectId, String keyword);
}