package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_telegram")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserTelegram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Telegram ID (chat_id)
    @Column(nullable = false, unique = true)
    private Long telegramId;
    @Column(unique = true)
    private String linkingToken;

    private String username;

    private boolean notificationsEnabled = true;
}