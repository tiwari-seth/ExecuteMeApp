package com.executeme.angeltelegramoauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartapi")
public record SmartApiProperties(
        String apiKey,
        String secretKey,
        String redirectUrl,
        String loginEndpoint
) {
    public SmartApiProperties {
        if (loginEndpoint == null || loginEndpoint.isBlank()) {
            loginEndpoint = "https://smartapi.angelone.in/publisher-login";
        }
    }
}
