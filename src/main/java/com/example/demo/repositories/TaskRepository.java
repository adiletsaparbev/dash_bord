package com.example.demo.repositories;

import com.example.demo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

<<<<<<< HEAD
    long countByProjectId(Long projectId);

    // Поиск по названию и/или тегам
    @Query("""
                SELECT DISTINCT t FROM Task t
                LEFT JOIN t.tags tag
                WHERE (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
                  AND (:tagIds IS NULL OR tag.id IN :tagIds)
            """)
    List<Task> searchTasks(
            @Param("title") String title,
            @Param("tagIds") List<Long> tagIds);
=======
    // Поиск по названию и/или тегам
    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN t.tags tag
        WHERE (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tagIds IS NULL OR tag.id IN :tagIds)
    """)
    List<Task> searchTasks(
            @Param("title") String title,
            @Param("tagIds") List<Long> tagIds
    );
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803

    // Задачи по исполнителю
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignees a WHERE a.user.id = :userId")
    List<Task> findByAssigneeUserId(@Param("userId") Long userId);

    // Все задачи по отделу (через проект → department)
    @Query("""
<<<<<<< HEAD
                SELECT t FROM Task t
                WHERE t.project.department.id = :departmentId
            """)
=======
        SELECT t FROM Task t
        WHERE t.project.department.id = :departmentId
    """)
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    List<Task> findByDepartmentId(@Param("departmentId") Long departmentId);

    // Просроченные задачи для шедулера
    @Query("""
<<<<<<< HEAD
                SELECT t FROM Task t
                WHERE t.dueDate < :today
                  AND t.status <> com.example.demo.enums.TaskStatus.DONE
            """)
=======
        SELECT t FROM Task t
        WHERE t.dueDate < :today
          AND t.status <> com.example.demo.enums.TaskStatus.DONE
    """)
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    List<Task> findOverdueTasks(@Param("today") LocalDate today);
}