package com.example.demo.services;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Image;
import com.example.demo.entity.Position;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PositionRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    // Добавьте в UserService.java

    private final PositionRepository positionRepository; // Не забудьте добавить в конструктор/RequiredArgsConstructor

    @Transactional
    public UserResponse updatePosition(String email, Long positionId) {
        User user = getUser(email);

        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Выбранная должность не существует"));

        user.setPosition(position);
        return toResponse(userRepository.save(user));
    }
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024; // 5 MB

    // =========================================================
    // 1. Получить информацию о себе
    // =========================================================
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String email) {
        User user = getUser(email);
        return toResponse(user);
    }

    // =========================================================
    // 2. Загрузить или заменить аватар
    // =========================================================
    @Transactional
    public UserResponse uploadAvatar(String email, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл обязателен");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Файл слишком большой. Максимум 5 МБ");
        }

        String mime = file.getContentType();
        if (mime == null || !ALLOWED_IMAGE_TYPES.contains(mime)) {
            throw new IllegalArgumentException(
                    "Недопустимый формат. Разрешены: JPEG, PNG, WEBP, GIF");
        }

        User user = getUser(email);

        Image avatar = user.getAvatar();
        if (avatar == null) {
            // Создаём новый объект Image
            avatar = new Image();
        }

        avatar.setName(file.getName());
        avatar.setOriginalFileName(file.getOriginalFilename());
        avatar.setContentType(mime);
        avatar.setSize(file.getSize());
        avatar.setBytes(file.getBytes());

        user.setAvatar(avatar);
        return toResponse(userRepository.save(user));
    }

    // =========================================================
    // 3. Получить байты аватара (для отдачи через контроллер)
    // =========================================================
    @Transactional(readOnly = true)
    public Image getAvatar(String email) {
        User user = getUser(email);

        if (user.getAvatar() == null) {
            throw new ResourceNotFoundException("Аватар не установлен");
        }

        return user.getAvatar();
    }

    // =========================================================
    // 4. Изменить имя и/или фамилию
    // =========================================================
    @Transactional
    public UserResponse updateName(String email, String name, String surname) {
        if ((name == null || name.isBlank()) && (surname == null || surname.isBlank())) {
            throw new IllegalArgumentException("Укажите имя или фамилию для обновления");
        }

        User user = getUser(email);

        if (name != null && !name.isBlank()) {
            user.setName(name.trim());
        }

        if (surname != null && !surname.isBlank()) {
            user.setSurname(surname.trim());
        }

        return toResponse(userRepository.save(user));
    }

    // =========================================================
    // Вспомогательные методы
    // =========================================================
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setName(u.getName());
        r.setSurname(u.getSurname());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole().name());
        r.setDepartmentName(u.getDepartment() != null ? u.getDepartment().getName() : null);
        r.setPositionName(u.getPosition() != null ? u.getPosition().getName() : null);
        r.setHasAvatar(u.getAvatar() != null);
        r.setCreatedAt(u.getCreatedAt());
        r.setActive(u.isActive());
        return r;
    }
}
