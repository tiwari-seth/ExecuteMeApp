package com.executeme.execution.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ExecutionSummaryView", description = "Aggregate execution-job counts by lifecycle status.")
public record ExecutionSummaryView(
        @Schema(description = "Pending job count", example = "10")
        long pending,
        @Schema(description = "Processing job count", example = "2")
        long processing,
        @Schema(description = "Successful job count", example = "88")
        long success,
        @Schema(description = "Failed job count", example = "5")
        long failed,
        @Schema(description = "Retry-pending job count", example = "1")
        long retryPending
) {
}
