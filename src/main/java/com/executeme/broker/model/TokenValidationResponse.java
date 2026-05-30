package com.executeme.broker.model;

public record TokenValidationResponse(
        boolean valid,
        String status,
        String message
) {
}
