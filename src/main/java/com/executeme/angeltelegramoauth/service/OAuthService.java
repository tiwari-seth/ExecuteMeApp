package com.executeme.angeltelegramoauth.service;

import com.executeme.angeltelegramoauth.config.SmartApiProperties;
import com.executeme.angeltelegramoauth.config.TokenSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    private static final String PURPOSE = "ANGEL_LOGIN";
    private final SmartApiProperties smartApiProperties;
    private final TokenSecurityProperties tokenSecurityProperties;
    private final Key signingKey;

    public OAuthService(SmartApiProperties smartApiProperties, TokenSecurityProperties tokenSecurityProperties) {
        this.smartApiProperties = smartApiProperties;
        this.tokenSecurityProperties = tokenSecurityProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                tokenSecurityProperties.jwtSigningSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createLoginUrl(long telegramUserId) {
        String state = createState(telegramUserId);
        return smartApiProperties.loginEndpoint()
                + "?api_key=" + encode(required(smartApiProperties.apiKey(), "SMARTAPI_API_KEY"))
                + "&redirect_url=" + encode(required(smartApiProperties.redirectUrl(), "SMARTAPI_REDIRECT_URL"))
                + "&state=" + encode(state);
    }

    public String createState(long telegramUserId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(tokenSecurityProperties.stateTtlMinutes() * 60L);
        return Jwts.builder()
                .claim("telegramUserId", telegramUserId)
                .claim("purpose", PURPOSE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public long validateState(String state) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(state)
                .getBody();

        String purpose = claims.get("purpose", String.class);
        if (!PURPOSE.equals(purpose)) {
            throw new IllegalArgumentException("Invalid OAuth state purpose");
        }
        Number telegramUserId = claims.get("telegramUserId", Number.class);
        if (telegramUserId == null) {
            throw new IllegalArgumentException("OAuth state is missing Telegram identity");
        }
        return telegramUserId.longValue();
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
