package com.executeme.angeltelegramoauth.service;

import com.executeme.angeltelegramoauth.domain.TelegramUser;
import com.executeme.angeltelegramoauth.repository.TelegramUserRepository;
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
}
