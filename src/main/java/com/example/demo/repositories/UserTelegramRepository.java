package com.example.demo.repositories;

import com.example.demo.entity.UserTelegram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTelegramRepository extends JpaRepository<UserTelegram, Long> {

    Optional<UserTelegram> findByUserId(Long userId);

    Optional<UserTelegram> findByTelegramId(Long telegramId);
}