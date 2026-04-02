//package com.example.demo.telegram;
//
//import com.example.demo.entity.User;
//import com.example.demo.repositories.UserRepository;
//import com.example.demo.services.TelegramService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/webhook/telegram")
//@RequiredArgsConstructor
//public class TelegramWebhookController {
//
//    private final TelegramService telegramService;
//    private final UserRepository userRepository;
//
//    @PostMapping
//    public void onUpdate(@RequestBody Map<String, Object> update) {
//
//        Map<String, Object> message = (Map<String, Object>) update.get("message");
//        if (message == null) return;
//
//        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
//
//        Long chatId = Long.valueOf(chat.get("id").toString());
//        String username = (String) chat.get("username");
//
//        String text = (String) message.get("text");
//
//        // ============================================
//        // /start команда
//        // ============================================
//        if (text != null && text.startsWith("/start")) {
//
//            // ❗ ВАЖНО: тут нужно связать с User
//            // пока сделаем временно (например по username)
//
//            User user = userRepository.findByEmail(username)
//                    .orElseThrow(() -> new RuntimeException("User не найден"));
//
//            telegramService.connect(user, chatId, username);
//
//            telegramService.sendMessage(chatId, "✅ Вы подключены к системе!");
//        }
//    }
//}
