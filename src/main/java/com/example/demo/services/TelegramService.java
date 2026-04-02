package com.example.demo.services;

import com.example.demo.entity.User;
import com.example.demo.entity.UserTelegram;
import com.example.demo.repositories.UserTelegramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class TelegramService {

    private final UserTelegramRepository repository;

    @Value("${telegram.bot.token}")
    private String botToken = "8739152722:AAFM0Sls149lFE2lktJrdq06xvw52GzCLp0"; // В реальной жизни так не храним, а используем application.properties или секреты

    private final RestTemplate restTemplate = new RestTemplate();
    private long lastUpdateId = 0; // Чтобы не обрабатывать одно и то же сообщение дважды

    // 1. Генерируем ссылку для пользователя
//    public String generateLink(User user) {
//        String token = UUID.randomUUID().toString();
//
//        UserTelegram entity = repository.findByUserId(user.getId())
//                .orElse(UserTelegram.builder().user(user).build());
//
//        entity.setLinkingToken(token);
//        repository.save(entity);
//
//        // Формируем ссылку (замени dash_bord_bot на имя своего бота)
//        return "https://t.me/dash_bord_bot?start=" + token;
//    }
//
//    // 2. Фоновый процесс: опрашиваем Telegram каждые 2 секунды (Long Polling)
//    @Scheduled(fixedRate = 2000)
//    public void pollUpdates() {
//        String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + (lastUpdateId + 1);
//
//        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
//        if (response == null || !(boolean) response.get("ok")) return;
//
//        List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
//
//        for (Map<String, Object> update : updates) {
//            lastUpdateId = ((Number) update.get("update_id")).longValue();
//
//            if (update.containsKey("message")) {
//                Map<String, Object> message = (Map<String, Object>) update.get("message");
//                String text = (String) message.get("text");
//                Long chatId = ((Number) ((Map) message.get("from")).get("id")).longValue();
//                String username = (String) ((Map) message.get("from")).get("username");
//
//                // Если сообщение начинается с /start, значит там может быть наш токен
//                if (text != null && text.startsWith("/start ")) {
//                    String token = text.substring(7); // Отрезаем "/start "
//
//                    // Ищем пользователя по токену
//                    repository.findAll().stream() // В реале лучше сделать метод в репозитории findByLinkingToken
//                            .filter(ut -> token.equals(ut.getLinkingToken()))
//                            .findFirst()
//                            .ifPresent(ut -> {
//                                ut.setTelegramId(chatId);
//                                ut.setUsername(username);
//                                ut.setLinkingToken(null); // Удаляем токен после использования
//                                ut.setNotificationsEnabled(true);
//                                repository.save(ut);
//
//                                sendMessage(chatId, "✅ Успешно! Ваш аккаунт привязан к системе.");
//                            });
//                }
//            }
//        }
//    }

    // ============================================
    // Привязка Telegram к User
    // ============================================
    public void connect(User user, Long telegramId, String username) {
        UserTelegram entity = repository.findByUserId(user.getId())
                .orElse(UserTelegram.builder().user(user).build());

        entity.setTelegramId(telegramId);
        entity.setUsername(username);
        entity.setNotificationsEnabled(true);
        repository.save(entity);
    }

    // ============================================
    // Включить / выключить уведомления
    // ============================================
    public void updateSettings(Long userId, boolean enabled) {
        UserTelegram entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Telegram не подключен"));

        entity.setNotificationsEnabled(enabled);
        repository.save(entity);
    }

    // ============================================
    // Отправка сообщения
    // ============================================
    public void sendMessage(Long telegramId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        // 1. Создаем заголовки и указываем, что отправляем JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Создаем тело запроса в виде Map (Spring сам конвертирует это в правильный JSON)
        Map<String, Object> body = Map.of(
                "chat_id", telegramId,
                "text", text
        );

        // 3. Упаковываем всё в HttpEntity
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 4. Отправляем запрос
        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            // Это поможет увидеть реальную ошибку в консоли, если она случится
            System.err.println("Ошибка при отправке в TG: " + e.getMessage());
            throw e;
        }
    }

    // ============================================
    // Отправка по user
    // ============================================
    public void sendToUser(User user, String message) {
        repository.findByUserId(user.getId())
                .filter(UserTelegram::isNotificationsEnabled)
                .ifPresent(tg -> sendMessage(tg.getTelegramId(), message));
    }
}
