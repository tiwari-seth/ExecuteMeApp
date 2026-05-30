package com.executeme.execution.repository;

import com.executeme.execution.domain.TradeExecution;
import com.executeme.execution.model.ExecutionStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {

    List<TradeExecution> findByTradeRequestIdOrderByIdAsc(Long tradeRequestId);

    List<TradeExecution> findByStatusOrderByCreatedAtAsc(ExecutionStatus status);

    long countByTradeRequestIdAndStatus(Long tradeRequestId, ExecutionStatus status);

    @Modifying
    @Query(value = """
            UPDATE trade_executions
            SET status = 'PROCESSING',
                attempt_count = attempt_count + 1,
                processing_started_at = :now,
                updated_at = :now
            WHERE id = :id
              AND status IN ('PENDING', 'RETRY_PENDING')
            """, nativeQuery = true)
    int claimPendingExecution(@Param("id") Long id, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            UPDATE trade_executions
            SET status = 'SUCCESS',
                broker_order_id = :brokerOrderId,
                failure_reason = NULL,
                completed_at = :now,
                updated_at = :now
            WHERE id = :id
              AND status = 'PROCESSING'
            """, nativeQuery = true)
    int markSuccess(@Param("id") Long id, @Param("brokerOrderId") String brokerOrderId, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            UPDATE trade_executions
            SET status = 'FAILED',
                failure_reason = :failureReason,
                completed_at = :now,
                updated_at = :now
            WHERE id = :id
              AND status = 'PROCESSING'
            """, nativeQuery = true)
    int markFailed(@Param("id") Long id, @Param("failureReason") String failureReason, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            UPDATE trade_executions
            SET status = 'RETRY_PENDING',
                failure_reason = :reason,
                updated_at = :now
            WHERE status = 'PROCESSING'
              AND processing_started_at < :staleBefore
            """, nativeQuery = true)
    int markStaleProcessingForRetry(
            @Param("staleBefore") Instant staleBefore,
            @Param("reason") String reason,
            @Param("now") Instant now
    );
}
