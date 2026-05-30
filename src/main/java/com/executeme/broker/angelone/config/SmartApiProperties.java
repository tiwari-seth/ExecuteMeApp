package com.executeme.broker.angelone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartapi")
public record SmartApiProperties(
        String apiKey,
        String secretKey,
        String redirectUrl,
        String loginEndpoint,
        String rootUrl,
        String clientLocalIp,
        String clientPublicIp,
        String macAddress,
        String userType,
        String sourceId
) {
    public SmartApiProperties {
        if (loginEndpoint == null || loginEndpoint.isBlank()) {
            loginEndpoint = "https://smartapi.angelone.in/publisher-login";
        }
        if (rootUrl == null || rootUrl.isBlank()) {
            rootUrl = "https://apiconnect.angelone.in";
        }
        if (clientLocalIp == null || clientLocalIp.isBlank()) {
            clientLocalIp = "127.0.0.1";
        }
        if (clientPublicIp == null || clientPublicIp.isBlank()) {
            clientPublicIp = "127.0.0.1";
        }
        if (macAddress == null || macAddress.isBlank()) {
            macAddress = "00:00:00:00:00:00";
        }
        if (userType == null || userType.isBlank()) {
            userType = "USER";
        }
        if (sourceId == null || sourceId.isBlank()) {
            sourceId = "WEB";
        }
    }
}
