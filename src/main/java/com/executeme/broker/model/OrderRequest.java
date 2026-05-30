package com.executeme.broker.model;

import java.math.BigDecimal;

public record OrderRequest(
        String userId,
        Long brokerSessionId,
        BrokerName brokerName,
        String symbol,
        String symbolToken,
        String exchange,
        String variety,
        String transactionType,
        String orderType,
        int quantity,
        String duration,
        BigDecimal price,
        BigDecimal triggerPrice,
        BigDecimal squareOff,
        BigDecimal stopLoss,
        String scripConsent,
        String productType
) {
}
