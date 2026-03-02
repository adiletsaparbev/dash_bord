package com.example.demo.services;

import com.example.demo.entity.*;
import com.example.demo.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Отправить уведомление
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

    // Получить все уведомления пользователя
    public List<Notification> getAll(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Только непрочитанные
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    // Количество непрочитанных (для бейджа)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Отметить как прочитанное
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    // Отметить все как прочитанные
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}