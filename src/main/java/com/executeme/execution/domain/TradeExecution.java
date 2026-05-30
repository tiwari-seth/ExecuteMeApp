package com.executeme.execution.domain;

import com.executeme.execution.model.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "trade_executions")
public class TradeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_request_id", nullable = false)
    private TradeRequest tradeRequest;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "broker_session_id", nullable = false)
    private Long brokerSessionId;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "broker_client_id", nullable = false, length = 100)
    private String brokerClientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @Column(name = "broker_order_id", length = 100)
    private String brokerOrderId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected TradeExecution() {
    }

    public TradeExecution(TradeRequest tradeRequest, Long userId, Long brokerSessionId,
                          String brokerName, String brokerClientId) {
        this.tradeRequest = tradeRequest;
        this.userId = userId;
        this.brokerSessionId = brokerSessionId;
        this.brokerName = brokerName;
        this.brokerClientId = brokerClientId;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TradeRequest getTradeRequest() {
        return tradeRequest;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBrokerSessionId() {
        return brokerSessionId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getBrokerClientId() {
        return brokerClientId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getBrokerOrderId() {
        return brokerOrderId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getProcessingStartedAt() {
        return processingStartedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
