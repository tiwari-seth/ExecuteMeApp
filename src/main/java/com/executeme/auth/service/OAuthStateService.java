package com.executeme.auth.service;

import com.executeme.auth.config.TokenSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class OAuthStateService {

    private static final String PURPOSE = "ANGEL_LOGIN";
    private final TokenSecurityProperties tokenSecurityProperties;
    private final Key signingKey;

    public OAuthStateService(TokenSecurityProperties tokenSecurityProperties) {
        this.tokenSecurityProperties = tokenSecurityProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                tokenSecurityProperties.jwtSigningSecret().getBytes(StandardCharsets.UTF_8)
        );
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
}
