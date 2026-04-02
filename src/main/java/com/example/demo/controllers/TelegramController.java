package com.example.demo.controllers;

import com.example.demo.dto.request.TelegramConnectRequest;
import com.example.demo.entity.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;
    private final UserRepository userRepository; // Добавляем репозиторий для поиска User

    // Помогательный метод для получения User из UserDetails
    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @PostMapping("/connect")
    public String connect(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody TelegramConnectRequest request) {

        User user = getUser(userDetails);
        telegramService.connect(user, request.getTelegramId(), request.getUsername());
        return "Telegram подключен";
    }

    @PostMapping("/settings")
    public String settings(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam boolean enabled) {

        User user = getUser(userDetails);
        telegramService.updateSettings(user.getId(), enabled);
        return "Настройки обновлены";
    }

    @PostMapping("/test")
    public String test(@AuthenticationPrincipal UserDetails userDetails) {

        User user = getUser(userDetails);
        telegramService.sendToUser(user, "✅ Тестовое сообщение из системы");
        return "Отправлено";
    }
//    @GetMapping("/get-link")
//    public String getLink(@AuthenticationPrincipal UserDetails userDetails) {
//        User user = getUser(userDetails);
//        return telegramService.generateLink(user);
//    }
}