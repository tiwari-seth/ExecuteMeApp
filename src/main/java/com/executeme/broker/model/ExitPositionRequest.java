package com.executeme.broker.model;

public record ExitPositionRequest(
        String userId,
        BrokerName brokerName,
        String symbol,
        String exchange,
        int quantity,
        String productType
) {
}
