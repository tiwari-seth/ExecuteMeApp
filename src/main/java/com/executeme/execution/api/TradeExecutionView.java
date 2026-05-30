package com.executeme.execution.api;

import com.executeme.execution.domain.TradeExecution;
import com.executeme.execution.model.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "TradeExecutionView", description = "Execution job for one user and one broker session.")
public record TradeExecutionView(
        @Schema(description = "Execution job id", example = "5001")
        Long id,
        @Schema(description = "Parent trade request id", example = "1001")
        Long tradeRequestId,
        @Schema(description = "Internal ExecuteMe user id", example = "7")
        Long userId,
        @Schema(description = "Broker session used by this execution", example = "12")
        Long brokerSessionId,
        @Schema(description = "Broker code", example = "ANGEL_ONE")
        String brokerName,
        @Schema(description = "Broker-side client/account id", example = "A123456")
        String brokerClientId,
        @Schema(description = "Execution lifecycle status", example = "PENDING")
        ExecutionStatus status,
        @Schema(description = "Broker order id after successful placement", example = "240530000123456")
        String brokerOrderId,
        @Schema(description = "Failure reason when execution fails")
        String failureReason,
        @Schema(description = "Number of worker attempts", example = "1")
        int attemptCount,
        @Schema(description = "When processing began")
        Instant processingStartedAt,
        @Schema(description = "When execution finished")
        Instant completedAt,
        @Schema(description = "Execution row creation time")
        Instant createdAt,
        @Schema(description = "Execution row last update time")
        Instant updatedAt
) {
    public static TradeExecutionView from(TradeExecution execution) {
        return new TradeExecutionView(
                execution.getId(),
                execution.getTradeRequest().getId(),
                execution.getUserId(),
                execution.getBrokerSessionId(),
                execution.getBrokerName(),
                execution.getBrokerClientId(),
                execution.getStatus(),
                execution.getBrokerOrderId(),
                execution.getFailureReason(),
                execution.getAttemptCount(),
                execution.getProcessingStartedAt(),
                execution.getCompletedAt(),
                execution.getCreatedAt(),
                execution.getUpdatedAt()
        );
    }
}
