package com.executeme.users.repository;

import com.executeme.users.domain.TelegramUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

    Optional<TelegramUser> findByTelegramUserId(Long telegramUserId);

    List<TelegramUser> findByActiveTrue();
}
