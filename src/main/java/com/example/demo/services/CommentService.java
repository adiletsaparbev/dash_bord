package com.example.demo.services;

import com.example.demo.dto.request.CommentRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Comment addComment(Long taskId, CommentRequest request, String authorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Comment comment = Comment.builder()
                .task(task)
                .author(author)
                .content(request.getContent())
                .build();
        comment = commentRepository.save(comment);

        // Уведомить всех исполнителей о новом комментарии
        task.getAssignees().forEach(a ->
                notificationService.send(a.getUser(), "NEW_COMMENT",
                        author.getFullName() + " прокомментировал задачу: " + task.getTitle(), task));

        // Обработать @упоминания
        parseMentions(comment, request.getContent());

        return comment;
    }

    public List<Comment> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // Парсим @username из текста
    private void parseMentions(Comment comment, String content) {
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String username = matcher.group(1);
            userRepository.findByEmail(username).ifPresent(user -> {
                notificationService.send(user, "NEW_COMMENT",
                        "Вас упомянули в комментарии к задаче: " + comment.getTask().getTitle(),
                        comment.getTask());
            });
        }
    }
}