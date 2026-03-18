package com.example.demo.services;

import com.example.demo.dto.response.NotificationResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.Role;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;

    // =========================================================
    // 1. Уведомление о перегрузке (>90%)
    // =========================================================

    // =========================================================
    // 2. Отправить уведомление одному пользователю (базовый метод)
    // =========================================================
    @Transactional
    public void send(User user, String type, String message, Task task) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .task(task)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // =========================================================
    // 3. Уведомить всех участников задачи (создатель + исполнители)
    // =========================================================
    @Transactional
    public void notifyTaskParticipants(Task task, String type, String message, User exclude) {
        if (task.getCreator() != null && !task.getCreator().getId().equals(exclude.getId())) {
            send(task.getCreator(), type, message, task);
        }
        if (task.getAssignees() != null) {
            task.getAssignees().stream()
                    .map(TaskAssignee::getUser)
                    .filter(u -> u != null && !u.getId().equals(exclude.getId()))
                    .forEach(u -> send(u, type, message, task));
        }
    }

    // =========================================================
    // 4. Проверка — уже отправляли TASK_OVERDUE сегодня?
    // =========================================================
    @Transactional(readOnly = true)
    public boolean wasOverdueNotificationSentToday(Long taskId, Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        return notificationRepository
                .existsByTaskIdAndUserIdAndTypeAndCreatedAtBetween(
                        taskId, userId, "TASK_OVERDUE", startOfDay, endOfDay);
    }

    // =========================================================
    // 5. Получение и чтение уведомлений
    // =========================================================
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markTaskNotificationsAsRead(Long taskId, Long userId) {
        List<Notification> notifications =
                notificationRepository.findByTaskIdAndUserIdAndIsReadFalse(taskId, userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // =========================================================
    // 6. Удаление (Только ADMIN)
    // =========================================================
    @Transactional
    public void delete(Long notificationId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Удалять уведомления может только администратор");
        }
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление не найдено"));
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllForUser(Long targetUserId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Удалять уведомления может только администратор");
        }
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(targetUserId);
        notificationRepository.deleteAll(all);
    }

    // =========================================================
    // Вспомогательный маппер
    // =========================================================
    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setMessage(n.getMessage());
        r.setRead(n.isRead());
        r.setCreatedAt(n.getCreatedAt());
        if (n.getTask() != null) {
            r.setTaskId(n.getTask().getId());
            r.setTaskTitle(n.getTask().getTitle());
        }
        return r;
    }
}
