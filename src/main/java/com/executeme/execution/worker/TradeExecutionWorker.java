package com.executeme.execution.worker;

import com.executeme.broker.client.BrokerClient;
import com.executeme.broker.model.BrokerName;
import com.executeme.broker.model.OrderRequest;
import com.executeme.broker.model.OrderResponse;
import com.executeme.execution.domain.TradeExecution;
import com.executeme.execution.domain.TradeRequest;
import com.executeme.execution.repository.TradeExecutionRepository;
import com.executeme.execution.service.TradeClaimService;
import com.executeme.execution.service.TradeResultService;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TradeExecutionWorker {

    private static final Logger log = LoggerFactory.getLogger(TradeExecutionWorker.class);

    private final TradeClaimService tradeClaimService;
    private final TradeResultService tradeResultService;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final BrokerClient brokerClient;

    public TradeExecutionWorker(TradeClaimService tradeClaimService,
                                TradeResultService tradeResultService,
                                TradeExecutionRepository tradeExecutionRepository,
                                BrokerClient brokerClient) {
        this.tradeClaimService = tradeClaimService;
        this.tradeResultService = tradeResultService;
        this.tradeExecutionRepository = tradeExecutionRepository;
        this.brokerClient = brokerClient;
    }

    @Async("tradeExecutor")
    public CompletableFuture<Void> process(Long executionId) {
        log.info("Attempting to claim trade execution: executionId={}", executionId);
        if (!tradeClaimService.claim(executionId)) {
            log.info("Trade execution claim skipped: executionId={}", executionId);
            return CompletableFuture.completedFuture(null);
        }

        try {
            TradeExecution execution = tradeExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("Trade execution not found: " + executionId));
            TradeRequest tradeRequest = execution.getTradeRequest();
            OrderRequest orderRequest = new OrderRequest(
                    execution.getUserId().toString(),
                    execution.getBrokerSessionId(),
                    BrokerName.valueOf(execution.getBrokerName().toUpperCase(Locale.ROOT)),
                    tradeRequest.getSymbol(),
                    tradeRequest.getSymbolToken(),
                    tradeRequest.getExchange(),
                    tradeRequest.getVariety().name(),
                    tradeRequest.getTransactionType().name(),
                    tradeRequest.getOrderType().name(),
                    tradeRequest.getQuantity(),
                    tradeRequest.getDuration().name(),
                    tradeRequest.getPrice(),
                    tradeRequest.getTriggerPrice(),
                    tradeRequest.getSquareOff(),
                    tradeRequest.getStopLoss(),
                    tradeRequest.getScripConsent(),
                    tradeRequest.getProductType().name()
            );

            log.info("Executing broker order: executionId={}, userId={}, broker={}",
                    executionId, execution.getUserId(), execution.getBrokerName());
            OrderResponse response = brokerClient.placeOrder(orderRequest);
            if (response.success()) {
                tradeResultService.markSuccess(executionId, response.brokerOrderId());
                log.info("Trade execution succeeded: executionId={}, brokerOrderId={}",
                        executionId, response.brokerOrderId());
            } else {
                tradeResultService.markFailed(executionId, response.message());
                log.warn("Trade execution failed: executionId={}, reason={}", executionId, response.message());
            }
        } catch (RuntimeException ex) {
            tradeResultService.markFailed(executionId, ex.getMessage());
            log.error("Trade execution crashed: executionId={}", executionId, ex);
        }
        return CompletableFuture.completedFuture(null);
    }
}
