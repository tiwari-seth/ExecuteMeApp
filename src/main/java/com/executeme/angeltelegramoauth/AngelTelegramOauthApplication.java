package com.executeme.angeltelegramoauth;

import com.executeme.angeltelegramoauth.config.AdminProperties;
import com.executeme.angeltelegramoauth.config.SmartApiProperties;
import com.executeme.angeltelegramoauth.config.TelegramBotProperties;
import com.executeme.angeltelegramoauth.config.TokenSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

@EnableScheduling
@SpringBootApplication
@Import(TelegramBotStarterConfiguration.class)
@EnableConfigurationProperties({
        SmartApiProperties.class,
        TelegramBotProperties.class,
        TokenSecurityProperties.class,
        AdminProperties.class
})
public class AngelTelegramOauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AngelTelegramOauthApplication.class, args);
    }
}
