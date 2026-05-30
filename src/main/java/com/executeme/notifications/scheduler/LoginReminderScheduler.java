package com.executeme.notifications.scheduler;

import com.executeme.telegram.bot.AngelTelegramBot;
import com.executeme.telegram.config.TelegramBotProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(AngelTelegramBot.class)
public class LoginReminderScheduler {

    private final AngelTelegramBot angelTelegramBot;
    private final TelegramBotProperties properties;

    public LoginReminderScheduler(AngelTelegramBot angelTelegramBot, TelegramBotProperties properties) {
        this.angelTelegramBot = angelTelegramBot;
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Kolkata")
    public void sendLoginReminders() {
        properties.allowedUserIdList().forEach(telegramUserId ->
                angelTelegramBot.sendLoginButton(
                        telegramUserId,
                        telegramUserId,
                        "Please login to Angel One for today's trading session."
                )
        );
    }
}
