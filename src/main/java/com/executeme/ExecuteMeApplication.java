package com.executeme;

import com.executeme.auth.config.AdminProperties;
import com.executeme.auth.config.TokenSecurityProperties;
import com.executeme.broker.angelone.config.SmartApiProperties;
import com.executeme.telegram.config.TelegramBotProperties;
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
public class ExecuteMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecuteMeApplication.class, args);
    }
}
