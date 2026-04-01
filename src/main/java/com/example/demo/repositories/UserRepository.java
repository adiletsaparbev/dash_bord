package com.example.demo.repositories;

import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByDepartmentId(Long departmentId);

    // Уже есть в AdminService — searchUsers
    @Query("SELECT u FROM User u WHERE " +
            "(:query IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:deptId IS NULL OR u.department.id = :deptId)")
    List<User> searchUsers(@Param("query") String query,
                           @Param("role") Role role,
                           @Param("deptId") Long deptId);

    // ↓ ДОБАВЬ ЭТИ МЕТОДЫ ↓

    // Фильтр по роли
    List<User> findByRole(Role role);

    // Фильтр по роли и отделу
    List<User> findByRoleAndDepartmentId(Role role, Long departmentId);

    // Пользователи, назначенные на задачи конкретного проекта
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN TaskAssignee ta ON ta.user.id = u.id
        JOIN Task t ON ta.task.id = t.id
        WHERE t.project.id = :projectId
    """)
    List<User> findByProjectId(@Param("projectId") Long projectId);

    // Для фильтра по проекту + роль + отдел (всё вместе)
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN TaskAssignee ta ON ta.user.id = u.id
        JOIN Task t ON ta.task.id = t.id
        WHERE (:projectId IS NULL OR t.project.id = :projectId)
          AND (:role IS NULL OR u.role = :role)
          AND (:departmentId IS NULL OR u.department.id = :departmentId)
    """)
    List<User> findEmployeesWithFilters(
            @Param("projectId") Long projectId,
            @Param("role") Role role,
            @Param("departmentId") Long departmentId
    );
}