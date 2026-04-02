package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TelegramViewController {

    @GetMapping("/telegram")
    public String telegramPage() {
        return "telegram"; // Spring будет искать файл src/main/resources/templates/telegram.html
    }
}