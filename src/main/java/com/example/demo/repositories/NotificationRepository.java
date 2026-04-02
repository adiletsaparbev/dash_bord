package com.example.demo.repositories;

import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

<<<<<<< HEAD
    // Уведомления по задаче (для удаления при удалении задачи)
    List<Notification> findByTaskId(Long taskId);

=======
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    // Все уведомления пользователя (по убыванию даты)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Только непрочитанные
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    // Количество непрочитанных
    long countByUserIdAndIsReadFalse(Long userId);

    // Непрочитанные уведомления по конкретной задаче (для авто-прочтения)
    @Query("SELECT n FROM Notification n WHERE n.task.id = :taskId AND n.user.id = :userId AND n.isRead = false")
    List<Notification> findByTaskIdAndUserIdAndIsReadFalse(
            @Param("taskId") Long taskId,
            @Param("userId") Long userId);

<<<<<<< HEAD
    // Проверка — отправляли ли уведомление типа TYPE по задаче пользователю за
    // период
=======
    // Проверка — отправляли ли уведомление типа TYPE по задаче пользователю за период
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
    // Используется планировщиком, чтобы не дублировать TASK_OVERDUE каждый день
    boolean existsByTaskIdAndUserIdAndTypeAndCreatedAtBetween(
            Long taskId,
            Long userId,
            String type,
            LocalDateTime from,
            LocalDateTime to);
}