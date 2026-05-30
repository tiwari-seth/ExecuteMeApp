package com.executeme.broker.model;

public record TokenRefreshResponse(
        boolean refreshed,
        String message
) {
}
