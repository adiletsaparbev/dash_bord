package com.example.demo.services;

import com.example.demo.dto.response.EmployeeResponse;
import com.example.demo.dto.response.EmployeeStatsResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.Role;
import com.example.demo.enums.TaskStatus;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // Максимальное кол-во задач для расчёта 100% загрузки
    private static final int MAX_TASKS = 10;
    // Порог перегрузки
    private static final double OVERLOAD_THRESHOLD = 90.0;

    // =========================================================
    // 1. GET /employees — список всех сотрудников с загрузкой
    // =========================================================
    public List<EmployeeResponse> getAllEmployees(String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);

        List<User> employees = getEmployeesByRole(currentUser, null, null, null);
        return employees.stream()
                .map(emp -> toEmployeeResponse(emp, currentUser))
                .toList();
    }

    // =========================================================
    // 2. GET /employees/filter — фильтрация сотрудников
    //    Параметры: departmentId, role, workload ("overloaded"), projectId
    // =========================================================
    public List<EmployeeResponse> filterEmployees(
            String currentUserEmail,
            Long departmentId,
            String roleName,
            String workload,    // "overloaded" — только перегруженные
            Long projectId) {

        User currentUser = getUser(currentUserEmail);

        Role roleFilter = roleName != null ? Role.valueOf(roleName.toUpperCase()) : null;

        List<User> employees;

        if (projectId != null) {
            // Если фильтр по проекту — используем специальный запрос
            employees = userRepository.findEmployeesWithFilters(projectId, roleFilter, departmentId);
        } else {
            employees = getEmployeesByRole(currentUser, departmentId, roleFilter, null);
        }

        List<EmployeeResponse> result = employees.stream()
                .map(emp -> toEmployeeResponse(emp, currentUser))
                .toList();

        // Фильтр по загрузке
        // Находим этот блок в методе filterEmployees
        if (workload != null && !workload.isEmpty()) {
            if ("overloaded".equalsIgnoreCase(workload)) {
                // Сохраняем логику для ключевого слова "overloaded" (> 90%)
                result = result.stream()
                        .filter(e -> e.getWorkloadPercent() > OVERLOAD_THRESHOLD)
                        .toList();
            } else if (workload.contains("-")) {
                // ВАРИАНТ 2: Диапазон (например, "70-90")
                try {
                    String[] parts = workload.split("-");
                    if (parts.length == 2) {
                        double min = Double.parseDouble(parts[0].trim());
                        double max = Double.parseDouble(parts[1].trim());
                        result = result.stream()
                                .filter(e -> e.getWorkloadPercent() >= min && e.getWorkloadPercent() <= max)
                                .toList();
                    }
                } catch (NumberFormatException e) {
                    // Если формат неверный, фильтр просто не применяется
                }
            } else {
                // ВАРИАНТ 1: Точное значение (например, "90")
                try {
                    double targetWorkload = Double.parseDouble(workload);
                    result = result.stream()
                            .filter(e -> e.getWorkloadPercent() == targetWorkload)
                            .toList();
                } catch (NumberFormatException e) {
                    // Если это не число, игнорируем
                }
            }
        }

        return result;
    }

    // =========================================================
    // 3. GET /employees/{id}/stats — детальная статистика
    // =========================================================
    public EmployeeStatsResponse getEmployeeStats(Long userId, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Проверяем права доступа
        checkAccessToEmployee(currentUser, employee);

        List<Task> allTasks = taskRepository.findByAssigneeUserId(userId);
        LocalDate today = LocalDate.now();

        int total = allTasks.size();
        int completed = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int active = (int) allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE).count();
        int overdue = (int) allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getStatus() != TaskStatus.DONE)
                .count();

        double overduePercent = total > 0 ? (overdue * 100.0 / total) : 0.0;

        // Среднее время выполнения: от createdAt до completedAt
        List<Task> doneTasks = taskRepository.findCompletedTasksByUserId(userId);
        Double avgHours = null;
        if (!doneTasks.isEmpty()) {
            double totalHours = doneTasks.stream()
                    .filter(t -> t.getCompletedAt() != null) // Защита от NPE
                    .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toHours())
                    .average()
                    .orElse(0.0);
            avgHours = Math.round(totalHours * 10.0) / 10.0;
        }

        EmployeeStatsResponse stats = new EmployeeStatsResponse();
        stats.setUserId(employee.getId());
        stats.setName(employee.getName());
        stats.setSurname(employee.getSurname());
        stats.setTotalTasks(total);
        stats.setCompletedTasks(completed);
        stats.setActiveTasks(active);
        stats.setOverdueTasks(overdue);
        stats.setOverduePercent(Math.round(overduePercent * 10.0) / 10.0);
        stats.setAvgCompletionHours(avgHours);

        return stats;
    }

    // =========================================================
    // Маппер User → EmployeeResponse + проверка перегрузки
    // =========================================================
    @Transactional
    public EmployeeResponse toEmployeeResponse(User emp, User currentUser) {
        List<Task> tasks = taskRepository.findByAssigneeUserId(emp.getId());
        LocalDate today = LocalDate.now();

        int total = tasks.size();
        int active = (int) tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE).count();
        int overdue = (int) tasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getStatus() != TaskStatus.DONE)
                .count();

        // Загрузка считается по весу сложности, а не по кол-ву задач
        // LOW=1, MEDIUM=2, HIGH=3. MAX_TASKS=10 — максимальный суммарный вес
        int totalWeight = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .mapToInt(t -> t.getComplexity() != null ? t.getComplexity().getWeight() : 1)
                .sum();
        double workload = Math.min((totalWeight * 100.0) / MAX_TASKS, 100.0);
        workload = Math.round(workload * 10.0) / 10.0;

        EmployeeResponse r = new EmployeeResponse();
        r.setId(emp.getId());
        r.setName(emp.getName());
        r.setSurname(emp.getSurname());
        r.setEmail(emp.getEmail());
        r.setRole(emp.getRole().name());
        r.setPositionName(emp.getPosition() != null ? emp.getPosition().getName() : null);
        r.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);
        r.setHasAvatar(emp.getAvatar() != null);
        r.setAvatarUrl(emp.getAvatar() != null ? "/api/employee/" + emp.getId() + "/avatar" : null);
        r.setTotalTasks(total);
        r.setActiveTasks(active);
        r.setOverdueTasks(overdue);
        r.setWorkloadPercent(workload);

        return r;
    }

    // =========================================================
    // Уведомить PM и Руководителя отдела о перегрузке
    // =========================================================

    // =========================================================
    // Получить сотрудников в зависимости от роли текущего юзера
    // =========================================================
    private List<User> getEmployeesByRole(User currentUser, Long departmentId,
                                          Role roleFilter, Long projectId) {
        List<User> all;

        if (currentUser.getRole() == Role.ADMIN) {
            if (departmentId != null) {
                all = userRepository.findByDepartmentId(departmentId);
            } else {
                all = userRepository.findAll();
            }
        } else if (currentUser.getRole() == Role.MANAGER) {
            // MANAGER видит только свой отдел
            Department dept = departmentRepository.findByManagerId(currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("У руководителя нет отдела"));
            all = userRepository.findByDepartmentId(dept.getId());
        } else {
            throw new AccessDeniedException("Нет доступа к списку сотрудников");
        }

        // Доп. фильтр по роли
        if (roleFilter != null) {
            Role finalRoleFilter = roleFilter;
            all = all.stream().filter(u -> u.getRole() == finalRoleFilter).toList();
        }

        return all;
    }

    // =========================================================
    // Проверка прав на просмотр статистики сотрудника
    // =========================================================
    private void checkAccessToEmployee(User currentUser, User targetEmployee) {
        if (currentUser.getRole() == Role.ADMIN) return;

        if (currentUser.getRole() == Role.MANAGER) {
            Department dept = departmentRepository.findByManagerId(currentUser.getId()).orElse(null);
            if (dept == null || targetEmployee.getDepartment() == null
                    || !dept.getId().equals(targetEmployee.getDepartment().getId())) {
                throw new AccessDeniedException("Можно просматривать статистику только своего отдела");
            }
            return;
        }

        // PM и TEAM могут смотреть только свою статистику
        if (!currentUser.getId().equals(targetEmployee.getId())) {
            throw new AccessDeniedException("Нет доступа к статистике другого пользователя");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    // =========================================================
    // Получить аватар сотрудника по id
    // =========================================================
    public Image getEmployeeAvatar(Long userId) {
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        if (employee.getAvatar() == null) {
            throw new ResourceNotFoundException("Аватар не установлен");
        }
        return employee.getAvatar();
    }
    @Transactional
    public double calculateWorkload(Long userId) {
        int totalWeight = taskRepository.findByAssigneeUserId(userId).stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .mapToInt(t -> t.getComplexity() != null ? t.getComplexity().getWeight() : 1)
                .sum();

        // MAX_TASKS = 10 теперь является максимальным весом
        // Пример: 3 задачи HIGH (вес 3) → totalWeight = 9 → (9/10)*100 = 90%
        return Math.min((totalWeight * 100.0) / MAX_TASKS, 100.0);
    }
    @Transactional
    public void sendOverloadNotificationIfNeeded(User employee, double workload) {
        if (employee == null) return;

        String fullName = employee.getName() + " " + (employee.getSurname() != null ? employee.getSurname() : "");
        String message = "Сотрудник " + fullName + " перегружен: " + String.format("%.1f", workload) + "% загрузки";

        // Защита от NullPointerException и пустых строк, если у юзера нет фамилии
        String searchKey = (employee.getSurname() != null && !employee.getSurname().trim().isEmpty())
                ? employee.getSurname()
                : employee.getName();

        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        Department dept = employee.getDepartment();
        if (dept == null) return;

        // 1. Уведомляем Руководителя отдела
        User manager = dept.getManager();
        if (manager != null) {
            notificationService.send(manager, "OVERLOAD", message, null);
        }

        // 2. Уведомляем ПМ-ов текущих проектов сотрудника
        taskRepository.findByAssigneeUserId(employee.getId()).stream()
                .map(Task::getProject)
                .filter(p -> p != null && p.getPm() != null)
                .map(Project::getPm)
                .distinct()
                .forEach(pm -> {
                    boolean alreadySentToPm = notificationRepository
                            .existsByUserIdAndTypeAndMessageContainingAndCreatedAtAfter(
                                    pm.getId(), "OVERLOAD", searchKey, dayAgo);

                    if (!alreadySentToPm) {
                        notificationService.send(pm, "OVERLOAD", message, null);
                    }
                });
    }

}
