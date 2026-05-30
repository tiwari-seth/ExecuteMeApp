package com.executeme.broker.model;

public record OrderResponse(
        boolean success,
        String brokerOrderId,
        String message
) {
}
