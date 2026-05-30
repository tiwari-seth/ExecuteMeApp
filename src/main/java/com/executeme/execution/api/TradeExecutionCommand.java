package com.executeme.execution.api;

import com.executeme.execution.model.OrderDuration;
import com.executeme.execution.model.OrderType;
import com.executeme.execution.model.OrderVariety;
import com.executeme.execution.model.ProductType;
import com.executeme.execution.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "TradeExecutionCommand", description = "Admin command for creating a trade request and execution jobs.")
public record TradeExecutionCommand(
        @Schema(description = "Broker trading symbol", example = "RELIANCE-EQ")
        @NotBlank String symbol,
        @Schema(description = "Broker symbol token required by Angel One order APIs", example = "2885")
        @NotBlank String symbolToken,
        @Schema(description = "Exchange code", example = "NSE")
        @NotBlank String exchange,
        @Schema(description = "Order variety. Defaults to NORMAL when omitted.", example = "NORMAL")
        OrderVariety variety,
        @Schema(description = "BUY or SELL", example = "BUY")
        @NotNull TransactionType transactionType,
        @Schema(description = "Order type", example = "MARKET")
        @NotNull OrderType orderType,
        @Schema(description = "Order quantity", example = "1")
        @Min(1) int quantity,
        @Schema(description = "Order duration. Defaults to DAY when omitted.", example = "DAY")
        OrderDuration duration,
        @Schema(description = "Order price. Use 0 for MARKET orders.", example = "0")
        BigDecimal price,
        @Schema(description = "Trigger price for stop-loss orders", example = "0")
        BigDecimal triggerPrice,
        @Schema(description = "Square-off value for supported order varieties", example = "0")
        BigDecimal squareOff,
        @Schema(description = "Stop-loss value for supported order varieties", example = "0")
        BigDecimal stopLoss,
        @Schema(description = "Optional scrip consent value required by some Angel One flows", example = "yes")
        String scripConsent,
        @Schema(description = "Broker product type", example = "INTRADAY")
        @NotNull ProductType productType,
        @Schema(description = "Internal ExecuteMe user ids to target. Empty means all active Angel One sessions.", example = "[1,2]")
        List<Long> userIds,
        @Schema(description = "Admin/user label that submitted the request", example = "admin")
        String createdBy
) {
}
