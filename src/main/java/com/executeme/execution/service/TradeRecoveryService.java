package com.executeme.execution.service;

import com.executeme.execution.repository.TradeExecutionRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeRecoveryService {

    private static final Duration STALE_PROCESSING_THRESHOLD = Duration.ofMinutes(15);
    private final TradeExecutionRepository tradeExecutionRepository;

    public TradeRecoveryService(TradeExecutionRepository tradeExecutionRepository) {
        this.tradeExecutionRepository = tradeExecutionRepository;
    }

    @Scheduled(fixedDelayString = "${execution.recovery-delay-ms:60000}")
    @Transactional
    public void retryStaleProcessingJobs() {
        Instant now = Instant.now();
        tradeExecutionRepository.markStaleProcessingForRetry(
                now.minus(STALE_PROCESSING_THRESHOLD),
                "Execution worker did not complete within " + STALE_PROCESSING_THRESHOLD.toMinutes() + " minutes",
                now
        );
    }
}
