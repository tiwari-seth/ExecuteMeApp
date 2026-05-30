package com.executeme.broker.session.service;

import java.time.Instant;

public record BrokerSessionCredentials(
        Long brokerSessionId,
        Long userId,
        String brokerName,
        String brokerClientId,
        String accessToken,
        String refreshToken,
        String feedToken,
        Instant tokenExpiresAt
) {
}
