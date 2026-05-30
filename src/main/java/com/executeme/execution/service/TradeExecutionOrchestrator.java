package com.executeme.execution.service;

import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.broker.session.service.BrokerSessionService;
import com.executeme.execution.api.ExecutionSummaryView;
import com.executeme.execution.api.TradeExecutionCommand;
import com.executeme.execution.api.TradeExecutionView;
import com.executeme.execution.api.TradeRequestView;
import com.executeme.execution.domain.TradeExecution;
import com.executeme.execution.domain.TradeRequest;
import com.executeme.execution.model.ExecutionStatus;
import com.executeme.execution.model.TradeRequestStatus;
import com.executeme.execution.repository.TradeExecutionRepository;
import com.executeme.execution.repository.TradeRequestRepository;
import com.executeme.execution.worker.TradeExecutionWorker;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TradeExecutionOrchestrator {

    private final BrokerSessionService brokerSessionService;
    private final TradeRequestRepository tradeRequestRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final TradeExecutionWorker tradeExecutionWorker;

    public TradeExecutionOrchestrator(BrokerSessionService brokerSessionService,
                                      TradeRequestRepository tradeRequestRepository,
                                      TradeExecutionRepository tradeExecutionRepository,
                                      TradeExecutionWorker tradeExecutionWorker) {
        this.brokerSessionService = brokerSessionService;
        this.tradeRequestRepository = tradeRequestRepository;
        this.tradeExecutionRepository = tradeExecutionRepository;
        this.tradeExecutionWorker = tradeExecutionWorker;
    }

    @Transactional
    public TradeRequestView submit(TradeExecutionCommand command) {
        List<BrokerSession> sessions = brokerSessionService.listExecutableAngelSessions(command.userIds());
        if (sessions.isEmpty()) {
            throw new IllegalArgumentException("No active Angel One broker sessions found for this request");
        }

        TradeRequest request = new TradeRequest(
                command.symbol(),
                command.symbolToken(),
                command.exchange(),
                command.variety(),
                command.transactionType(),
                command.orderType(),
                command.quantity(),
                command.duration(),
                command.price(),
                command.triggerPrice(),
                command.squareOff(),
                command.stopLoss(),
                command.scripConsent(),
                command.productType(),
                command.createdBy() == null || command.createdBy().isBlank() ? "admin" : command.createdBy()
        );
        request.markProcessing();
        TradeRequest savedRequest = tradeRequestRepository.save(request);
        List<TradeExecution> executions = sessions.stream()
                .map(session -> new TradeExecution(
                        savedRequest,
                        session.getUserId(),
                        session.getId(),
                        session.getBrokerName(),
                        session.getBrokerClientId()
                ))
                .toList();
        List<TradeExecution> savedExecutions = tradeExecutionRepository.saveAll(executions);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                savedExecutions.forEach(execution -> tradeExecutionWorker.process(execution.getId()));
            }
        });
        return TradeRequestView.from(savedRequest, savedExecutions.stream().map(TradeExecutionView::from).toList());
    }

    @Transactional(readOnly = true)
    public TradeRequestView getRequest(Long tradeRequestId) {
        TradeRequest request = tradeRequestRepository.findById(tradeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Trade request not found: " + tradeRequestId));
        return TradeRequestView.from(request, listExecutions(tradeRequestId));
    }

    @Transactional(readOnly = true)
    public List<TradeExecutionView> listExecutions(Long tradeRequestId) {
        return tradeExecutionRepository.findByTradeRequestIdOrderByIdAsc(tradeRequestId).stream()
                .map(TradeExecutionView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExecutionSummaryView summary(Long tradeRequestId) {
        return new ExecutionSummaryView(
                tradeExecutionRepository.countByTradeRequestIdAndStatus(tradeRequestId, ExecutionStatus.PENDING),
                tradeExecutionRepository.countByTradeRequestIdAndStatus(tradeRequestId, ExecutionStatus.PROCESSING),
                tradeExecutionRepository.countByTradeRequestIdAndStatus(tradeRequestId, ExecutionStatus.SUCCESS),
                tradeExecutionRepository.countByTradeRequestIdAndStatus(tradeRequestId, ExecutionStatus.FAILED),
                tradeExecutionRepository.countByTradeRequestIdAndStatus(tradeRequestId, ExecutionStatus.RETRY_PENDING)
        );
    }

    @Transactional
    public void reconcileRequestStatus(Long tradeRequestId) {
        TradeRequest request = tradeRequestRepository.findById(tradeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Trade request not found: " + tradeRequestId));
        ExecutionSummaryView summary = summary(tradeRequestId);
        if (summary.pending() + summary.processing() + summary.retryPending() > 0) {
            return;
        }
        if (summary.failed() == 0) {
            request.markCompleted(TradeRequestStatus.COMPLETED);
        } else if (summary.success() == 0) {
            request.markCompleted(TradeRequestStatus.FAILED);
        } else {
            request.markCompleted(TradeRequestStatus.PARTIALLY_FAILED);
        }
    }
}
