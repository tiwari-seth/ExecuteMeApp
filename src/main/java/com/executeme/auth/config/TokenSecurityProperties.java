package com.executeme.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token-security")
public record TokenSecurityProperties(
        String jwtSigningSecret,
        String encryptionSecret,
        int stateTtlMinutes
) {
    public TokenSecurityProperties {
        if (stateTtlMinutes <= 0) {
            stateTtlMinutes = 5;
        }
    }
}
