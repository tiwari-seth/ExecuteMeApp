package com.executeme.angeltelegramoauth.config;

import java.util.List;
import java.util.Arrays;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
        String username,
        String token,
        String allowedUserIds
) {
    public boolean hasCredentials() {
        return username != null && !username.isBlank() && token != null && !token.isBlank();
    }

    public boolean isAllowed(long telegramUserId) {
        return allowedUserIdList().contains(telegramUserId);
    }

    public List<Long> allowedUserIdList() {
        if (allowedUserIds == null || allowedUserIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedUserIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::parseLong)
                .toList();
    }
}
