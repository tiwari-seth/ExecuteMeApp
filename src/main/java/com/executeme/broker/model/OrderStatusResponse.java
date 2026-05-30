package com.executeme.broker.model;

public record OrderStatusResponse(
        String orderId,
        String status,
        String message
) {
}
