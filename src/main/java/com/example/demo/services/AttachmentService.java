package com.example.demo.services;

import com.example.demo.entity.*;
import com.example.demo.enums.EntityType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.AttachmentRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Загрузить файл
    public Attachment upload(MultipartFile file, EntityType entityType,
                             Long entityId, String uploaderEmail) throws IOException {
        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Создаём папку если нет
        Path dir = Paths.get(uploadDir, entityType.name().toLowerCase(), entityId.toString());
        Files.createDirectories(dir);

        // Уникальное имя файла
        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dir.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = Attachment.builder()
                .entityType(entityType)
                .entityId(entityId)
                .uploader(uploader)
                .fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        return attachmentRepository.save(attachment);
    }

    // Скачать файл
    public Resource download(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не найден"));

        Path path = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("Физический файл не найден");
        }
        return resource;
    }

    // Список файлов
    public List<Attachment> getAttachments(EntityType entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    // Удалить файл
    public void delete(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не найден"));

        // Удаляем физически
        Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        attachmentRepository.delete(attachment);
    }
}