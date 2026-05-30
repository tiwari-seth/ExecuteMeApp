package com.executeme.execution.service;

import com.executeme.execution.repository.TradeExecutionRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeResultService {

    private static final int MAX_FAILURE_LENGTH = 4000;
    private final TradeExecutionRepository tradeExecutionRepository;

    public TradeResultService(TradeExecutionRepository tradeExecutionRepository) {
        this.tradeExecutionRepository = tradeExecutionRepository;
    }

    @Transactional
    public void markSuccess(Long executionId, String brokerOrderId) {
        tradeExecutionRepository.markSuccess(executionId, brokerOrderId, Instant.now());
    }

    @Transactional
    public void markFailed(Long executionId, String failureReason) {
        tradeExecutionRepository.markFailed(executionId, truncate(failureReason), Instant.now());
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_FAILURE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_FAILURE_LENGTH);
    }
}
