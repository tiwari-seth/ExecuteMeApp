package com.executeme.execution.repository;

import com.executeme.execution.domain.TradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
}
