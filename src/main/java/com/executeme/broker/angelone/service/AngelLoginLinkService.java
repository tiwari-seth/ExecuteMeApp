package com.executeme.broker.angelone.service;

import com.executeme.auth.service.OAuthStateService;
import com.executeme.broker.angelone.config.SmartApiProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class AngelLoginLinkService {

    private final SmartApiProperties smartApiProperties;
    private final OAuthStateService oauthStateService;

    public AngelLoginLinkService(SmartApiProperties smartApiProperties, OAuthStateService oauthStateService) {
        this.smartApiProperties = smartApiProperties;
        this.oauthStateService = oauthStateService;
    }

    public String createLoginUrl(long telegramUserId) {
        String state = oauthStateService.createState(telegramUserId);
        return smartApiProperties.loginEndpoint()
                + "?api_key=" + encode(required(smartApiProperties.apiKey(), "SMARTAPI_API_KEY"))
                + "&redirect_url=" + encode(required(smartApiProperties.redirectUrl(), "SMARTAPI_REDIRECT_URL"))
                + "&state=" + encode(state);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String required(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(envName + " must be configured");
        }
        return value;
    }
}
