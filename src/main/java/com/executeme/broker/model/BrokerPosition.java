package com.executeme.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "BrokerPosition", description = "Broker position snapshot.")
public record BrokerPosition(
        @Schema(description = "Internal user id as a string", example = "7")
        String userId,
        @Schema(description = "Broker code", example = "ANGEL_ONE")
        BrokerName brokerName,
        @Schema(description = "Trading symbol", example = "RELIANCE-EQ")
        String symbol,
        @Schema(description = "Exchange code", example = "NSE")
        String exchange,
        @Schema(description = "Net quantity", example = "10")
        int quantity,
        @Schema(description = "Average price", example = "2860.50")
        BigDecimal averagePrice,
        @Schema(description = "Last traded price", example = "2875.20")
        BigDecimal lastTradedPrice,
        @Schema(description = "Current profit/loss", example = "147.00")
        BigDecimal pnl
) {
}
