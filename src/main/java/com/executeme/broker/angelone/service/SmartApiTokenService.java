package com.executeme.broker.angelone.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class SmartApiTokenService {

    public AngelTokenDetails decode(String authToken) {
        DecodedJWT jwt = JWT.decode(authToken);
        String clientId = firstPresent(
                jwt.getClaim("username").asString(),
                jwt.getClaim("clientcode").asString(),
                jwt.getClaim("sub").asString()
        );
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Angel auth token does not contain username, clientcode, or sub");
        }

        Instant expiresAt = jwt.getExpiresAt() == null ? null : jwt.getExpiresAt().toInstant();
        return new AngelTokenDetails(clientId, expiresAt);
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public record AngelTokenDetails(String brokerClientId, Instant expiresAt) {
    }
}
