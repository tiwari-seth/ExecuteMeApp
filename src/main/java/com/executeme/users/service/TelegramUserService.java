package com.executeme.users.service;

import com.executeme.users.domain.TelegramUser;
import com.executeme.users.repository.TelegramUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    public TelegramUserService(TelegramUserRepository telegramUserRepository) {
        this.telegramUserRepository = telegramUserRepository;
    }

    @Transactional
    public TelegramUser upsertAllowedUser(long telegramUserId, String username, String fullName) {
        TelegramUser user = telegramUserRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> new TelegramUser(telegramUserId, username, fullName));
        user.updateProfile(username, fullName);
        return telegramUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<TelegramUser> listActiveUsers() {
        return telegramUserRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public TelegramUser getRequiredByTelegramUserId(long telegramUserId) {
        return telegramUserRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new IllegalArgumentException("Telegram user is not registered: " + telegramUserId));
    }
}
