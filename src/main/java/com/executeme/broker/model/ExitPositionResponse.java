package com.executeme.broker.model;

public record ExitPositionResponse(
        boolean success,
        String brokerOrderId,
        String message
) {
}
