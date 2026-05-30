package com.executeme.execution.controller;

import com.executeme.execution.api.ExecutionSummaryView;
import com.executeme.execution.api.TradeExecutionCommand;
import com.executeme.execution.api.TradeExecutionView;
import com.executeme.execution.api.TradeRequestView;
import com.executeme.execution.service.TradeExecutionOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/trades")
@Tag(name = "Trade Execution")
@SecurityRequirement(name = "basicAuth")
public class AdminTradeExecutionController {

    private final TradeExecutionOrchestrator tradeExecutionOrchestrator;

    public AdminTradeExecutionController(TradeExecutionOrchestrator tradeExecutionOrchestrator) {
        this.tradeExecutionOrchestrator = tradeExecutionOrchestrator;
    }

    @PostMapping("/execute")
    @Operation(
            summary = "Submit a trade request",
            description = "Creates a trade request, creates one execution job per eligible broker session, and dispatches async workers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trade request accepted",
                    content = @Content(schema = @Schema(implementation = TradeRequestView.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no executable broker sessions found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public TradeRequestView execute(@Valid @RequestBody TradeExecutionCommand command) {
        return tradeExecutionOrchestrator.submit(command);
    }

    @GetMapping("/{tradeRequestId}")
    @Operation(
            summary = "Get trade request",
            description = "Returns a trade request with its execution jobs and reconciles the aggregate request status first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trade request returned",
                    content = @Content(schema = @Schema(implementation = TradeRequestView.class))),
            @ApiResponse(responseCode = "400", description = "Trade request was not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public TradeRequestView tradeRequest(
            @Parameter(description = "Trade request id", example = "1001")
            @PathVariable Long tradeRequestId
    ) {
        tradeExecutionOrchestrator.reconcileRequestStatus(tradeRequestId);
        return tradeExecutionOrchestrator.getRequest(tradeRequestId);
    }

    @GetMapping("/{tradeRequestId}/executions")
    @Operation(
            summary = "List execution jobs",
            description = "Returns all user/broker execution jobs for a trade request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Execution jobs returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TradeExecutionView.class)))),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public List<TradeExecutionView> executions(
            @Parameter(description = "Trade request id", example = "1001")
            @PathVariable Long tradeRequestId
    ) {
        return tradeExecutionOrchestrator.listExecutions(tradeRequestId);
    }

    @GetMapping("/{tradeRequestId}/summary")
    @Operation(
            summary = "Get execution summary",
            description = "Returns aggregate execution counts by lifecycle status for a trade request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Execution summary returned",
                    content = @Content(schema = @Schema(implementation = ExecutionSummaryView.class))),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public ExecutionSummaryView summary(
            @Parameter(description = "Trade request id", example = "1001")
            @PathVariable Long tradeRequestId
    ) {
        return tradeExecutionOrchestrator.summary(tradeRequestId);
    }
}
