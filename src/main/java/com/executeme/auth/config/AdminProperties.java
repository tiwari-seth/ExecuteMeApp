package com.executeme.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public record AdminProperties(
        String username,
        String password
) {
    public boolean enabled() {
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
