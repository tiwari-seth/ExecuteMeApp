package com.executeme.execution.service;

import com.executeme.execution.repository.TradeExecutionRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeClaimService {

    private final TradeExecutionRepository tradeExecutionRepository;

    public TradeClaimService(TradeExecutionRepository tradeExecutionRepository) {
        this.tradeExecutionRepository = tradeExecutionRepository;
    }

    @Transactional
    public boolean claim(Long executionId) {
        return tradeExecutionRepository.claimPendingExecution(executionId, Instant.now()) == 1;
    }
}
