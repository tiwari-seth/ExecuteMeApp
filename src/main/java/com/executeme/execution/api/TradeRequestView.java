package com.executeme.execution.api;

import com.executeme.execution.domain.TradeRequest;
import com.executeme.execution.model.OrderDuration;
import com.executeme.execution.model.OrderType;
import com.executeme.execution.model.OrderVariety;
import com.executeme.execution.model.ProductType;
import com.executeme.execution.model.TradeRequestStatus;
import com.executeme.execution.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "TradeRequestView", description = "Admin trade intent plus generated execution jobs.")
public record TradeRequestView(
        @Schema(description = "Trade request id", example = "1001")
        Long id,
        @Schema(description = "Broker trading symbol", example = "RELIANCE-EQ")
        String symbol,
        @Schema(description = "Broker symbol token", example = "2885")
        String symbolToken,
        @Schema(description = "Exchange code", example = "NSE")
        String exchange,
        @Schema(description = "Order variety", example = "NORMAL")
        OrderVariety variety,
        @Schema(description = "BUY or SELL", example = "BUY")
        TransactionType transactionType,
        @Schema(description = "Order type", example = "MARKET")
        OrderType orderType,
        @Schema(description = "Order quantity", example = "1")
        int quantity,
        @Schema(description = "Order duration", example = "DAY")
        OrderDuration duration,
        @Schema(description = "Order price", example = "0")
        BigDecimal price,
        @Schema(description = "Trigger price for stop-loss orders", example = "0")
        BigDecimal triggerPrice,
        @Schema(description = "Square-off value", example = "0")
        BigDecimal squareOff,
        @Schema(description = "Stop-loss value", example = "0")
        BigDecimal stopLoss,
        @Schema(description = "Optional scrip consent")
        String scripConsent,
        @Schema(description = "Product type", example = "INTRADAY")
        ProductType productType,
        @Schema(description = "Aggregate request status", example = "PROCESSING")
        TradeRequestStatus status,
        @Schema(description = "Admin/user label that submitted the request", example = "admin")
        String createdBy,
        @Schema(description = "Request creation time")
        Instant createdAt,
        @Schema(description = "Execution jobs generated from this request")
        List<TradeExecutionView> executions
) {
    public static TradeRequestView from(TradeRequest request, List<TradeExecutionView> executions) {
        return new TradeRequestView(
                request.getId(),
                request.getSymbol(),
                request.getSymbolToken(),
                request.getExchange(),
                request.getVariety(),
                request.getTransactionType(),
                request.getOrderType(),
                request.getQuantity(),
                request.getDuration(),
                request.getPrice(),
                request.getTriggerPrice(),
                request.getSquareOff(),
                request.getStopLoss(),
                request.getScripConsent(),
                request.getProductType(),
                request.getStatus(),
                request.getCreatedBy(),
                request.getCreatedAt(),
                executions
        );
    }
}
